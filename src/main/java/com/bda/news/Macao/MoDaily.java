package com.bda.news.Macao;

import com.bda.common.*;
import com.bda.news.PostNews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/25
 * @description: 澳门日报
 **/
public class MoDaily {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Log log = LogFactory.getLog(MoDaily.class);
    private static final String pattern = "yyyy-MM-dd HH:mm:ss.S";

    public static List<PostNews> crawNews() {
        LocalDate now = LocalDate.now().minusMonths(3);
        int page = 1;
        boolean inThreeMonth = true;
        List<PostNews> postNewsList = Lists.newArrayList();
        while (inThreeMonth) {
            String url = "https://app.modaily.cn/app_if/getArticles?columnId=102&page=" + page + "&lastFileId=9323197";
            log.info(url);
            String search = RequestUtil.commonGet(url, Maps.newHashMap());
            JsonNode root;
            try {
                root = mapper.readTree(search);
            } catch (JsonProcessingException e) {
               continue;
            }
            if (root.isMissingNode()) continue;
            JsonNode nodeList = JsonNodeUtil.parseArray(root, "list");
            if (nodeList == null) continue;
            for (JsonNode node : nodeList) {
                String time = JsonNodeUtil.parseElement(node, String.class, "publishtime");
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null && now.isAfter(localDate)) {
                    inThreeMonth = false;
                    break;
                }
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                String title = JsonNodeUtil.parseElement(node, String.class, "title");
                String author = JsonNodeUtil.parseElement(node, String.class, "edittername");
                String href = JsonNodeUtil.parseElement(node, String.class, "url");
                JsonNode imageList = JsonNodeUtil.parseArray(node, "pic_list_title");
                String imgUrl = null;
                if (imageList != null && !imageList.isEmpty()) imgUrl = imageList.get(0).textValue();
                String contentUrl = JsonNodeUtil.parseElement(node, String.class, "contentUrl");
                String json = RequestUtil.commonGet(contentUrl, Maps.newHashMap());
                String content = null;
                try {
                    String contentHtml = JsonNodeUtil.parseElement(mapper.readTree(json), String.class, "content");
                    if (contentHtml != null) {
                        content = Jsoup.parse(contentHtml).text();
                    }
                } catch (JsonProcessingException e) {
                    continue;
                }
                PostNews postNews = PostNews.builder()
                        .title(title).author(author)
                        .content(content).time(time)
                        .url(href).imgUrl(imgUrl)
                        .language(PostNews.CN_LANGUAGE)
                        .build();
                log.info(postNews);
                postNewsList.add(postNews);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    continue;
                }
            }
            page++;
        }
        return postNewsList;
    }

    public static void main(String[] args) throws JsonProcessingException {
        List<PostNews> postNews = crawNews();
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\MoDaily\\MoDaily.json",postNews);
    }

}
