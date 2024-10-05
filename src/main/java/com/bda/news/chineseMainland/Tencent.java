package com.bda.news.chineseMainland;

import com.bda.common.*;
import com.bda.news.PostNews;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.*;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class Tencent {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String pattern = "yyyy-MM-dd HH:mm:ss";
    private final static String patternCommon = "yyyy-MM-dd";
    private static final Logger log = LogManager.getLogger(Tencent.class);

    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        LocalDate localDate = LocalDate.now().minusDays(3);
        // 请求体数据
        Map<String, Object> formData = Maps.newHashMap();
        Map<String, Object> header = Maps.newHashMap();
        formData.put("page", 0);
        formData.put("query", keyWord);
        log.info("===="+keyWord+"======");
        formData.put("is_pc", 1);
        formData.put("hippy_custom_version", 24);
        formData.put("search_type", "all");
        formData.put("search_count_limit", 10);
        formData.put("appver", "15.5_qqnews_7.1.80");
        formData.put("suid", "8QIf3n5f7YQauj/Q5As=");
        header.put("Cookie", "pgv_pvid=389672557"+(int)(10 * Math.random())+"; " +
                             "eas_sid=71t7K2H5A0U0G965b7d574V0s"+(int)(10 * Math.random())+";" +
                             " pgv_info=ssid=s609997270"+(int)(10 * Math.random())+"; " +
                             "_qimei_uuid42=18a01022c2d10034f58f3f961e7d7368996585a78"+random()+";" +
                             " pac_uid=0_xKDWTiC6cR85D;" +
                             " suid=user_0_xKDWTiC6cR85D; " +
                             "current-city-name=jx;" +
                             " _qimei_q32=d18b116b0b550b2e245e78299b3686d"+(int)(10 * Math.random())+"; " +
                             "_qimei_q36=6fa56b59c9a9b59e192cc0e7300018f18a0"+(int)(10 * Math.random())+"; " +
                             "_qimei_fingerprint=8957ce8bc73e9be966a9c2f1880b09e"+random()+";" +
                             " _qimei_h38=2db8bf19f58f3f961e7d73680200000b718a0"+(int)(10 * Math.random()));
        String bsUrl = "https://i.news.qq.com/gw/pc_search/result";
        String infos = RequestUtil.commonPost(bsUrl, formData, Maps.newHashMap());
        // 获取搜索列新闻信息
        JsonNode tree = objectMapper.readTree(infos);
        List<PostNews> res = parse(tree);
        Long totalNum = JsonNodeUtil.parseElement(tree, Long.class, "total_num");
        while (totalNum != null && 10L * ((int) formData.get("page") + 1) < totalNum) {
            Thread.sleep(5000+(long)(Math.random() * 500));
            formData.put("page", (int) formData.get("page") + 1);
            infos = RequestUtil.commonPost(bsUrl, formData, Maps.newHashMap());
            // 获取搜索列新闻信息
            tree = objectMapper.readTree(infos);
            for (PostNews postNews : parse(tree)) {
                LocalDate date = TimeUtil.parseDate(postNews.getTime(), patternCommon);
                if (date==null || localDate.isAfter(date)){
                    totalNum = null;
                }else {
                    res.add(postNews);
                }
            }
        }
        Thread.sleep(1000 * 60);
        return res;
    }

    @SneakyThrows
    public static List<PostNews> parse(JsonNode tree) {
        List<PostNews> postNewsList = Lists.newArrayList();
        JsonNode secList = JsonNodeUtil.parseNode(tree, "secList");
        if (secList == null) return Lists.newArrayList();
        Iterator<JsonNode> elements = secList.elements();
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            // 解析基本本信息
            JsonNode newsList = JsonNodeUtil.parseArray(element, "newsList");
            if (newsList == null) continue;
            Iterator<JsonNode> newsIt = newsList.elements();
            while (newsIt.hasNext()) {
                JsonNode news = newsIt.next();
                String title = JsonNodeUtil.parseElement(news, String.class, "title");
                String time = JsonNodeUtil.parseElement(news, String.class, "time");
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                String url = JsonNodeUtil.parseElement(news, String.class, "url");
                String author = JsonNodeUtil.parseElement(news, String.class, "card", "chlname");
                String shareUrl = JsonNodeUtil.parseElement(news, String.class, "shareUrl");
                JsonNode imgArray = JsonNodeUtil.parseArray(news, "bigImage");
                String imgUrl = null;
                if (imgArray!=null && !imgArray.isEmpty()) imgUrl = imgArray.get(0).textValue();
                if (StringUtils.isBlank(shareUrl)) continue;
                String content = parseNewsContent(shareUrl);
                PostNews postNews = PostNews.builder()
                        .title(title).time(time)
                        .author(author).url(url)
                        .content(content).imgUrl(imgUrl)
                        .language(PostNews.CN_LANGUAGE)
                        .build();
                log.info(postNews);
                postNewsList.add(postNews);
                Thread.sleep(5000);
            }
        }
        return postNewsList;
    }

    @SneakyThrows
    public static String parseNewsContent(String url) {
        String content = "";
        String html = RequestUtil.commonGet(url, Maps.newHashMap());
        Element head = Jsoup.parse(html).head();
        Elements scripts = head.select("script");
        for (Element script : scripts) {
            String scriptContent = script.html().trim();
            // 判断是否包含"window.DATA ="，以此作为定位JSON对象的依据
            if (scriptContent.contains("window.DATA")) {
                // 提取JSON字符串部分（去掉window.DATA等前缀）
                int jsonStartIndex = scriptContent.indexOf('{');
                int jsonEndIndex = scriptContent.lastIndexOf('}') + 1;

                if (jsonStartIndex != -1) {
                    String jsonString = scriptContent.substring(jsonStartIndex, jsonEndIndex);
                    // 解析JSON
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    // 获取originContent中的text字段
                    String jsonText = JsonNodeUtil.parseElement(jsonNode, String.class, "originContent", "text");
                    if (jsonText != null) {
                        content = Jsoup.parse(jsonText).text();
                    }
                }
            }
        }
        return content;
    }


    public static List<PostNews> crawNewsByList(List<String> keyWords) {
        List<PostNews> postNewsList = Lists.newArrayList();
        keyWords.forEach(keyWord -> postNewsList.addAll(crawNews(keyWord)));
        return postNewsList;
    }

    public static char random(){
        // 创建一个Random对象
        Random random = new Random();

        // 生成随机小写字母
        return (char) ('a' + random.nextInt(26));
    }

    public static void main(String[] args) {
        List<String> keyWords = Lists.newArrayList("香港庆祝国庆节", "香港与大湾区发展", "香港人才引进与培养", "澳门回归25周年", "香港", "澳门");
        List<PostNews> postNews = crawNewsByList(keyWords);
        FileUtil.write("C:\\Users\\arane\\Desktop\\webCrawler\\src\\main\\resources\\news\\resource\\" + Tencent.class.getSimpleName() + ".json", postNews);
    }

}
