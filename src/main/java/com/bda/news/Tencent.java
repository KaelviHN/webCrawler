package com.bda.news;

import com.bda.common.PostNews;
import com.bda.common.JsonNodeUtil;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class Tencent {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public List<PostNews> crawNews(String keyWord) {
        // 请求体数据
        Map<String, Object> formData = Maps.newHashMap();
        formData.put("page", 0);
        formData.put("query", keyWord);
        formData.put("is_pc", 1);
        formData.put("hippy_custom_version", 24);
        formData.put("search_type", "all");
        formData.put("search_count_limit", 10);
        formData.put("appver", "15.5_qqnews_7.1.80");
        formData.put("suid", "8QIf3n5f7YQauj/Q5As=");
        String bsUrl = "https://i.news.qq.com/gw/pc_search/result";
        String infos  = RequestUtil.commonPost(bsUrl,formData);
        // 获取搜索列新闻信息
        JsonNode tree = objectMapper.readTree(infos);
        List<PostNews> res = parse(tree);
        String totalNum = JsonNodeUtil.parseElement(tree, "total_num");
        while (StringUtils.isNotBlank(totalNum) && 10 * ((int)formData.get("page") + 1) < Integer.parseInt(totalNum)) {
            Thread.sleep(500);
            formData.put("page",(int)formData.get("page")+1);
            infos  = RequestUtil.commonPost(bsUrl,formData);
            // 获取搜索列新闻信息
            tree = objectMapper.readTree(infos);
            res.addAll(parse(tree));
        }
        return res;
    }

    @SneakyThrows
    public List<PostNews> parse(JsonNode tree) {
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
                String title = JsonNodeUtil.parseElement(news, "title");
                String time = JsonNodeUtil.parseElement(news, "time");
                String url = JsonNodeUtil.parseElement(news, "url");
                String author = JsonNodeUtil.parseElement(news, "card", "chlname");
                String shareUrl = JsonNodeUtil.parseElement(news, "shareUrl");
                String content = parseNewsContent(shareUrl);
                PostNews postNews = PostNews.builder()
                        .title(title).time(time)
                        .author(author).url(url)
                        .content(content)
                        .build();
                postNewsList.add(postNews);
            }
        }
        return postNewsList;
    }

    @SneakyThrows
    public String parseNewsContent(String url) {
        String content = "";
        String html = RequestUtil.commonGet(url,Maps.newHashMap());
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
                    String jsonText = JsonNodeUtil.parseElement(jsonNode, "originContent", "text");
                    if (jsonText != null) {
                        content = Jsoup.parse(jsonText).text();
                    }
                }
            }
        }
        return content;
    }



    public static void main(String[] args) {
        Tencent tencent = new Tencent();
        System.out.println(tencent.crawNews("澳门选举"));
    }


}
