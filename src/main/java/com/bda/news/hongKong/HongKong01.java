package com.bda.news.hongKong;

import com.bda.common.*;
import com.bda.news.PostNews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.propertyeditors.URLEditor;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author: anran.ma
 * @created: 2024/9/27
 * @description:
 **/
public class HongKong01 {
    private static final Logger log = LogManager.getLogger(HongKong01.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<PostNews> crawNews(String host, int port) {
        LocalDate range = LocalDate.now().minusMonths(3);
        List<PostNews> postNewsList = Lists.newArrayList();
        boolean canContinue = true;
        String url = "https://www.hk01.com/latest";
        String offset = "";
        while (canContinue) {
            Document firstSearch = null;
            JsonNode secondSearch = null;
            try {
                if (StringUtils.isNotBlank(offset)) {
                    String sUrl = "https://web-data.api.hk01.com/v2/feed/category/0?offset=" + offset + "&bucketId=00000";
                    log.info(sUrl);
                    secondSearch = mapper.readTree(RequestUtil.proxyGet(sUrl, host, port, Maps.newHashMap()));
                } else {
                    log.info(url);
                    firstSearch = Jsoup.parse(RequestUtil.proxyGet(url, host, port, Maps.newHashMap()));
                }
            } catch (IOException e) {
                continue;
            }
            Element nextData = null;
            Elements newsList = new Elements();
            // 获取更多信息的offset && 元素列表
            if (firstSearch != null) {
                newsList = firstSearch.getElementsByClass("card-title break-words");
                nextData = firstSearch.getElementById("__NEXT_DATA__");
                // 处理首次的nextOffset
                if (nextData == null) {
                    canContinue = false;
                } else {
                    try {
                        JsonNode tree = mapper.readTree(nextData.html());
                        offset = JsonNodeUtil.parseElement(tree, String.class, "props", "initialProps", "pageProps", "nextOffset");
                    } catch (JsonProcessingException e) {
                        canContinue = false;
                    }
                }
            } else {
                offset = JsonNodeUtil.parseElement(secondSearch, String.class, "nextOffset");
            }
            // 判断是否有后续数据
            if (StringUtils.isBlank(offset)){
                log.info(secondSearch);
                canContinue = false;
            }
            // 遍历首次
            for (Element news : newsList) {
                // href
                String href = JsoupUtil.getFirstElementByClass(news, "card-title break-words", "href");
                PostNews postNews = crawNewsDetail(href, range, host, port);
                // 获取不到数据
                if (postNews == null) continue;
                // 超过三月
                if (StringUtils.isNotBlank(postNews.getTime()) && "-1".equals(postNews.getTime())) {
                    canContinue = false;
                    break;
                }
                postNewsList.add(postNews);
            }
            if (secondSearch != null) {
                JsonNode array = JsonNodeUtil.parseArray(secondSearch, "items");
                if (array == null) continue;
                for (JsonNode newsInfo : array) {
                    String href = JsonNodeUtil.parseElement(newsInfo, String.class, "data", "canonicalUrl");
                    PostNews postNews = null;
                    if (StringUtils.isNotBlank(href)) {
                        postNews = crawNewsDetail(href, range, host, port);
                    }
                    if (postNews == null) {
                        href = JsonNodeUtil.parseElement(newsInfo, String.class, "data", "publishUrl");
                        if (StringUtils.isNotBlank(href)) postNews = crawNewsDetail(href, range, host, port);
                    }
                    // 获取不到数据
                    if (postNews == null) continue;
                    // 超过三月
                    if (StringUtils.isNotBlank(postNews.getTime()) && "-1".equals(postNews.getTime())) {
                        canContinue = false;
                        break;
                    }
                    postNewsList.add(postNews);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {

            }
        }
        return postNewsList;
    }

    public static PostNews crawNewsDetail(String href, LocalDate range, String host, int port) {
        if (href == null) return null;
        if (!href.contains("https://www.hk01.com")) href = "https://www.hk01.com" + href;
        log.info(href);
        Document detail;
        try {
            Map<String, Object> props = Maps.newHashMap();
            props.put("Cookie","hk01_annonymous_id=2fde92dd-7c8a-4264-8fb6-8f2211f1f2ea; hk01_asp_type=anonymous; hk01_segment_id=K1320; cf_clearance=yiP0z_2Whz60QZuozo4XJ1ZtU_5f15DzkkHZJmFaR7E-1727402387-1.2.1.1-1FFRlWsoMvGWq8zucbm8_VDa9zwuc9DPUu6tG6Uh89THXkXWopKuzgY1Js6qJUpIBsx9SA99T2GrweOCOjtDp7GoqMuHImUMMDAQW0rtDImgSXRTng6ybxFOixh4s7lmszmn..OcTaXFCAj7Ihl0rrnq3lrYy90W2dYr95.npAxw_a7RGyxgQmKjLfZH2ZO_xe1CqiYnxVxLS4Um03874vuGEWVd1QF9wJibjcgrdGZeK4wSqJD7.8HVQ_YRPZ9oPmHXPyvt63i1LwuEIsuD5fc7a9bviwOWhqZ4fkAhMv181tCsH5lRSK4bOJ5lhOJhHmZCjdIpvzvXPcNf6VHQ.B0UgU8F01TrORsLLjaUw9bZccfwOrPavKSa3L0Sszx.Amw7hEAm8FQp8CffOgwOfnGJ.CV37rOm9D1YvltWaPeJNXyBRwgpW1YKqQcogGuF");
            detail = Jsoup.parse(RequestUtil.proxyGet(href, host, port, props));
        } catch (IOException e) {
            return null;
        }
        // time
        String time = JsoupUtil.selectFirst(detail, "meta[property=article:published_time]", "content");
        LocalDate localDate = TimeUtil.parseDate(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (localDate != null && localDate.isBefore(range)) return PostNews.builder().time("-1").build();
        if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
        // title
        String title = detail.title();
        // author
        String author = JsoupUtil.selectFirst(detail, "meta[name=author]", "content");
        if (author == null) author = "香港01";
        // imgUrl
        String imgUrl = JsoupUtil.selectFirst(detail, "meta[property=og:image]", "content");
        //content
        String contentHeader = JsoupUtil.getFirstElementByClass(detail, "border-primary mb-6 block border-l-4 pl-3 md:mb-8", null);
        Elements words = detail.getElementsByClass("break-words");
        StringBuilder content = new StringBuilder();
        if (StringUtils.isNotBlank(contentHeader)) content.append(contentHeader);
        for (Element word : words) {
            content.append(word.text());
        }
        PostNews postNews = PostNews.builder()
                .title(title).imgUrl(imgUrl)
                .author(author).content(content.toString())
                .url(href).language(PostNews.CN_LANGUAGE)
                .time(time)
                .build();
        log.info(postNews);
        return postNews;
    }

    public static void main(String[] args) throws IOException {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\" + HongKong01.class.getSimpleName()+".json",
                postNews);
    }
}
