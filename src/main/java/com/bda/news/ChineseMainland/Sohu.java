package com.bda.news.ChineseMainland;

import com.bda.common.FileUtil;
import com.bda.common.JsonNodeUtil;
import com.bda.common.PostNews;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.yaml.snakeyaml.util.UriEncoder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/21
 * @description:
 **/
public class Sohu {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        HashMap<String, Object> header = Maps.newHashMap();
        header.put("referer", "https://search.sohu.com/");
        List<PostNews> postNewsList = Lists.newArrayList();
        Boolean isEnd = false;
        int idx = 0;
        while (isEnd != null && !isEnd) {
            JsonNode searchJson = objectMapper.readTree(RequestUtil.commonGet("https://search.sohu.com/search/meta?keyword="+UriEncoder.encode(keyWord)+
                                                                              "&terminalType=pc&spm-pre=smpc.csrpage.0.0.1727055547497f333Dny&SUV=240621103231SR2X&" +
                                                                              "from="+idx+"&size=10" +
                                                                              "&searchType=news&queryType=outside&queryId=17270580300005r6z010&pvId=1727055547497f333Dny" +
                                                                              "&refer=https%253A%2F%2Fnews.sohu.com%2F&spm=smpc.csrpage.0.0.1727055547497f333Dny&maxL=15", header));
            JsonNode news = JsonNodeUtil.parseArray(searchJson, "data", "news");
            isEnd = JsonNodeUtil.parseElement(searchJson, Boolean.class, "data", "esEnd");
            if (news == null) return Lists.newArrayList();
            Iterator<JsonNode> elements = news.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                String author = JsonNodeUtil.parseElement(element, String.class, "authorName");
                String title = JsonNodeUtil.parseElement(element, String.class, "title");
                Long time = JsonNodeUtil.parseElement(element, Long.class, "postTime");
                String url = JsonNodeUtil.parseElement(element, String.class, "url");
                String html = RequestUtil.commonGet(url, header);
                Element contentTag = Jsoup.parse(html).getElementById("mp-editor");
                String content = contentTag != null ? contentTag.text() : "";
                PostNews postNews = PostNews.builder()
                        .author(author).title(title)
                        .time(PostNews.patternTime(String.valueOf(time)))
                        .content(content).url(url)
                        .build();
                postNewsList.add(postNews);
            }
            idx++;
            Thread.sleep(5000);
        }
        return postNewsList;

    }

    public static void main(String[] args) {
        List<PostNews> postNewsList = crawNews("澳门");
        postNewsList.addAll(crawNews("香港"));
        FileUtil.writeHistory("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\Sohu",postNewsList,"Sohu.json");
    }
}
