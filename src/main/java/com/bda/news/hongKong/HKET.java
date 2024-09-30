package com.bda.news.hongKong;

import com.bda.common.*;
import com.bda.news.PostNews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: anran.ma
 * @created: 2024/9/27
 * @description:
 **/
@Log4j2
public class HKET {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String pattern = "HH:mm yyyy/MM/dd";


    public static List<PostNews> crawNews(String host, int port) {
        LocalDate range = LocalDate.now().minusMonths(3);
        List<PostNews> postNewsList = Lists.newArrayList();
        boolean isNotEnd = true;
        int p = 1;
        while (isNotEnd && p <= 20) {
            String url = "https://inews.hket.com/sran001/%E5%85%A8%E9%83%A8" + (p > 1 ? "?p=" + p : "");
            log.info(url);
            Document search;
            try {
                search = Jsoup.parse(RequestUtil.proxyGet(url, host, port, Maps.newHashMap()));
            } catch (IOException e) {
                continue;
            }
            Elements newsList = search.getElementsByClass("ellipsis");
            for (Element element : newsList) {
                String href = element.attr("href");
                PostNews postNews = crawNewsDetail(href, range, host, port);
                // 获取不到数据
                if (postNews == null || StringUtils.isBlank(postNews.getAuthor()) || StringUtils.isBlank(postNews.getContent()))
                    continue;
                // 超过三月
                if (StringUtils.isNotBlank(postNews.getTime()) && "-1".equals(postNews.getTime())) {
                    isNotEnd = false;
                    break;
                }
                log.info(postNews);
                postNewsList.add(postNews);
            }
            p++;
        }
        return postNewsList;
    }

    public static PostNews crawNewsDetail(String href, LocalDate range, String host, int port) {
        Document detail;
        try {
            if (!href.contains("hket")) href = "https://invest.hket.com" + href;
            detail = Jsoup.parse(RequestUtil.proxyGet(href, host, port, Maps.newHashMap()));
        } catch (IOException e) {
            return null;
        }
        if (detail.getElementById("root") != null) {
            HashMap<String, Object> params = Maps.newHashMap();
            String[] keys = href.split("/");
            for (int i = 1; i < keys.length; i++) {
                if (keys[i - 1].equals("article")) params.put("id", keys[i]);
            }
            JsonNode tree;
            try {
                HashMap<String, Object> header = Maps.newHashMap();
                header.put("content-type", "application/json");
                tree = mapper.readTree(RequestUtil.proxyPostJson("https://invest.hket.com/content-api-middleware/content", host, port, params, header));
            } catch (Exception e) {
                return null;
            }
            String time = JsonNodeUtil.parseElement(tree, String.class, "lastModifiedDate");
            if (StringUtils.isBlank(time)) JsonNodeUtil.parseElement(tree, String.class, "displayDate");
            LocalDate localDate = TimeUtil.parseDate(time, pattern);
            if (localDate != null && localDate.isBefore(range)) return PostNews.builder().time("-1").build();
            if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
            String title = JsonNodeUtil.parseElement(tree, String.class, "headline", "main");
            JsonNode authors = JsonNodeUtil.parseArray(tree, "authors");
            StringBuilder author = new StringBuilder();
            if (authors != null)
                authors.forEach(item -> author.append(item.textValue() == null ? JsonNodeUtil.parseElement(item, String.class, "name") : item.textValue()).append(","));
            if (StringUtils.isBlank(author.toString())) {
                JsonNode writers = JsonNodeUtil.parseArray(tree, "writers");
                if (writers != null)
                    writers.forEach(item -> author.append(item.textValue() == null ? JsonNodeUtil.parseElement(item, String.class, "name") : item.textValue()).append(","));
            }
            if (author.length() > 0) {
                author.delete(author.length() - 1, author.length());
            }
            String contentHtml = JsonNodeUtil.parseElement(tree, String.class, "content", "partial");
            if (contentHtml == null) contentHtml = JsonNodeUtil.parseElement(tree, String.class, "content", "full");
            if (contentHtml == null) contentHtml = JsonNodeUtil.parseElement(tree, String.class, "content", "lead");
            String content = null;
            String imgUrl = null;
            if (StringUtils.isNotBlank(contentHtml)) {
                Document document = Jsoup.parse(contentHtml);
                Elements imgs = document.getElementsByTag("img").attr("src", "");
                Optional<String> first = imgs.stream().map(Element::text).filter(StringUtils::isNotBlank).findFirst();
                imgUrl = first.orElse("");
                content = document.text();
            }
            if (StringUtils.isNotBlank(contentHtml) && StringUtils.isBlank(imgUrl)) {
                String regex = "src=\"([^\"]+\\.(jpg|jpeg|png|gif|bmp|svg|webp))\"";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(contentHtml);
                if (matcher.find()) {
                    imgUrl = matcher.group(1);
                }
            }
            if (StringUtils.isBlank(contentHtml))
                content = JsonNodeUtil.parseElement(tree, String.class, "extraInformation", "seo", "meta", "description");
            return PostNews.builder()
                    .title(title).content(content)
                    .author(String.valueOf(author)).time(time)
                    .language(PostNews.CN_LANGUAGE).url(href)
                    .imgUrl(imgUrl)
                    .build();
        }
        String title = detail.title();
        String author = JsoupUtil.selectFirst(detail, "meta[name=author]", "content");
        String imgUrl = JsoupUtil.selectFirst(detail, "meta[property=og:image]", "content");
        if (StringUtils.isBlank(imgUrl)) {
            Elements imgs = detail.getElementsByTag("img").attr("src", "");
            Optional<String> first = imgs.stream().map(Element::text).filter(StringUtils::isNotBlank).findFirst();
            imgUrl = first.orElse(imgUrl);
        }
        String content = JsoupUtil.getFirstElementByClass(detail, "article-detail-content-container", null);
        if (StringUtils.isBlank(content))
            content = JsoupUtil.getFirstElementByClass(detail, "meta[name=description]", "content");
        if (StringUtils.isBlank(content)) return null;
        Element element = detail.selectFirst("script[type=application/ld+json]");
        if (element == null) return null;
        String time = null;
        try {
            JsonNode json = mapper.readTree(element.html());
            time = JsonNodeUtil.parseElement(json, String.class, "dateModified");
            if (time == null) JsonNodeUtil.parseElement(json, String.class, "datePublished");
            LocalDate localDate = TimeUtil.parseDate(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            if (localDate != null && localDate.isBefore(range)) return PostNews.builder().time("-1").build();
            if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
        } catch (JsonProcessingException e) {
            return null;
        }
        return PostNews.builder()
                .url(href).content(content)
                .author(author).title(title)
                .imgUrl(imgUrl).time(time)
                .language(PostNews.CN_LANGUAGE)
                .build();
    }

    public static void main(String[] args) throws IOException {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\" + HKET.class.getSimpleName() + ".json",
                postNews);
    }
}
