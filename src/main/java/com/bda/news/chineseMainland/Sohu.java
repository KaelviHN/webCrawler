package com.bda.news.chineseMainland;

import com.bda.common.FileUtil;
import com.bda.common.JsonNodeUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.yaml.snakeyaml.util.UriEncoder;

import java.time.LocalDate;
import java.util.*;

/**
 * @author: anran.ma
 * @created: 2024/9/21
 * @description:
 **/
public class Sohu {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String pattern = "yyyy-MM-dd HH:mm";
    private static final Logger log = LogManager.getLogger(Sohu.class);

    public static List<PostNews> crawNews(String keyWord) {
        LocalDate range = LocalDate.now().minusMonths(3);
        HashMap<String, Object> header = Maps.newHashMap();
        header.put("referer", "https://search.sohu.com/");
        List<PostNews> postNewsList = Lists.newArrayList();
        Boolean isEnd = false;
        int idx = 0;
        while (isEnd != null && !isEnd) {
            JsonNode searchJson = null;
            try {
                long time = Calendar.getInstance().getTime().getTime();
                searchJson = objectMapper.readTree(RequestUtil.commonGet("https://search.sohu.com/search/meta?" +
                                                                         "keyword=" + UriEncoder.encode(keyWord) +
                                                                         "&terminalType=pc&spm-pre=smpc.csrpage.0.0." + time + "rZHCzna" +
                                                                         "&SUV=" + time + generateRandomString() +
                                                                         "&from=" + idx +
                                                                         "&size=10" +
                                                                         "&searchType=news" +
                                                                         "&queryType=outside" +
                                                                         //"aaZ04" +
                                                                         "&queryId=" + (time - 80211) + generateRandomString() +
                                                                         "&pvId=" + (time - 80211) + "rZHCzna" +
                                                                         "&refer=https%253A%2F%2Fnews.sohu.com%2F" +
                                                                         "&spm=smpc.csrpage.0.0." + time + "rZHCzna" +
                                                                         "&maxL=15", header));
            } catch (JsonProcessingException e) {
                continue;
            }
            JsonNode news = JsonNodeUtil.parseArray(searchJson, "data", "news");
            isEnd = JsonNodeUtil.parseElement(searchJson, Boolean.class, "data", "esEnd");
            if (news == null) return postNewsList;
            Iterator<JsonNode> elements = news.elements();
            while (elements.hasNext()) {
                JsonNode element = elements.next();
                String author = JsonNodeUtil.parseElement(element, String.class, "authorName");
                String imgUrl = JsonNodeUtil.parseElement(element, String.class, "cover");
                String title = JsonNodeUtil.parseElement(element, String.class, "title");
                String time = JsonNodeUtil.parseElement(element, String.class, "postTime");
                time = PostNews.patternTime(time);
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null && localDate.isBefore(range)) {
                    isEnd = true;
                    break;
                }
                String url = JsonNodeUtil.parseElement(element, String.class, "url");
                String html = RequestUtil.commonGet(url, header);
                Element contentTag = Jsoup.parse(html).getElementById("mp-editor");
                String content = contentTag != null ? contentTag.text() : "";
                PostNews postNews = PostNews.builder()
                        .author(author).title(title)
                        .time(time).content(content)
                        .url(url).language(PostNews.CN_LANGUAGE)
                        .imgUrl(imgUrl)
                        .build();
                postNewsList.add(postNews);
                log.info(postNews);
            }
            idx++;
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
        return postNewsList;
    }

    public static List<PostNews> crawNewsByList(List<String> keyWords) {
        List<PostNews> postNewsList = Lists.newArrayList();
        keyWords.forEach(keyWord -> {
            postNewsList.addAll(crawNews(keyWord));
            try {
                Thread.sleep(50000);
            } catch (InterruptedException ignored) {
            }
        });
        return postNewsList;
    }
    private static String generateRandomString() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(5);

        for (int i = 0; i < 5; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        List<String> keyWords = Lists.newArrayList(
                "香港庆祝国庆节", "香港与大湾区发展", "香港人才引进与培养",
                "澳门回归25周年", "香港", "澳门");
        List<PostNews> postNews = crawNewsByList(keyWords);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\" + Sohu.class.getSimpleName() + ".json", postNews);
    }
}
