package com.bda.news.Macao;

import com.bda.common.JsoupUtil;
import com.bda.common.RequestUtil;
import com.bda.news.PostNews;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author: anran.ma
 * @created: 2024/9/24
 * @description:澳门力报
 **/
public class Exmoo {
    public static String crawNews(String proxyHost, int proxyPort) {
        ArrayList<@Nullable Object> objects = Lists.newArrayList();
        int page = 1;
        String url = "https://www.exmoo.com/hot" + (page == 1 ? "" : "p" + page);
        try {
            Document search = Jsoup.parse(RequestUtil.proxyGet(url, proxyHost, proxyPort));
            // 读取热门列表
            Elements hotList = search.getElementsByClass("hot-list-item");
            for (Element element : hotList) {
                // 获取连接
                Element urlTag = element.selectFirst("a");
                if (urlTag == null) continue;
                String href = urlTag.attr("href");
                if (StringUtils.isBlank(href)) continue;
                Document news = Jsoup.parse(RequestUtil.proxyGet(href, proxyHost, proxyPort));
                String title = JsoupUtil.selectFirst("meta[property=og:title]", news);
                String time = JsoupUtil.getFirstElementByClass("article-pub-date", news);
                String views = JsoupUtil.getFirstElementByClass("article-viewed", news);
                StringBuilder content = new StringBuilder();
                Elements elements = news.getElementsByClass("article-content-p");
                elements.forEach(content::append);
                PostNews postNews = PostNews.builder()
                        .author("澳门力报").time(time)
                        .title(title).views(views)
                        .content(content.toString())
                        .build();

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public static void main(String[] args) {
        crawNews("127.0.0.1", 7890);
    }
}
