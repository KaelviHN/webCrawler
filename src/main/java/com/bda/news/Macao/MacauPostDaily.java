package com.bda.news.Macao;

import com.bda.common.FileUtil;
import com.bda.common.JsoupUtil;
import com.bda.common.RequestUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/26
 * @description: 澳门邮报(英文)
 **/
public class MacauPostDaily {
    private static final Log log = LogFactory.getLog(MacauPostDaily.class);
    private static final String pattern = "yyyy-MM-dd HH:mm";

    public static List<PostNews> crawNews(String host, int port) {
        int page = 0;
        LocalDate rangeDate = LocalDate.now().minusMonths(3);
        List<PostNews> postNewsList = Lists.newArrayList();
        boolean inThreeMonth = true;
        while (inThreeMonth) {
            String url = "https://www.macaupostdaily.com/news/list" + (page > 0 ? "?page=" + page + "&per-page=15" : "");
            log.info(url);
            Document search = null;
            try {
                search = Jsoup.parse(RequestUtil.proxyGet(url, host, port, Maps.newHashMap()));
            } catch (IOException e) {
                continue;
            }
            Elements newsList = search.getElementsByClass("hot-news-list-item sm d-flex align-items-start justify-content-center text-decoration-none pb-4 mb-4");
            for (Element element : newsList) {
                if (StringUtils.isBlank(element.attr("href"))) continue;
                String href = "https://www.macaupostdaily.com" + element.attr("href");
                String title = JsoupUtil.getFirstElementByClass(element, "introduce overflow-hidden d-none d-md-block truncate-to-3-lines", null);
                String imgUrl = JsoupUtil.getFirstElementByClass(element, "img-fluid", "src");
                Document detail;
                try {
                    detail = Jsoup.parse(RequestUtil.proxyGet(href, host, port, Maps.newHashMap()));
                } catch (IOException e) {
                    continue;
                }
                String time = JsoupUtil.getFirstElementByClass(detail, "date mr-2 mr-xl-3", null);
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null && rangeDate.isAfter(localDate)) {
                    inThreeMonth = false;
                    break;
                }
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                String authorTxt = JsoupUtil.getFirstElementByClass(detail, "author mr-2 mr-xl-3", null);
                String author = null;
                if (StringUtils.isNotBlank(authorTxt)) author = authorTxt.replaceFirst("BY", "").trim();
                String content = JsoupUtil.getFirstElementByClass(detail, "content", null);
                PostNews postNews = PostNews.builder()
                        .author(author).time(time)
                        .content(content).language(PostNews.EN_LANGUAGE)
                        .url(href).title(title)
                        .imgUrl(imgUrl)
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

    public static void main(String[] args) {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\MacauPostDaily\\MacauPostDaily.json",
                postNews);
    }
}
