package com.bda.news.Macao;

import com.bda.common.FileUtil;
import com.bda.common.JsoupUtil;
import com.bda.common.RequestUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/26
 * @description: 澳门商报
 **/

public class MacauBusiness {
    private static final List<String> urls = Lists.newArrayList(
            "https://www.macaubusiness.com/category/mna/mna-macau/",
            "https://www.macaubusiness.com/category/mna/greater-bay/",
            "https://www.macaubusiness.com/category/mna/china/"
    );
    private static final Logger log = LogManager.getLogger(MacauBusiness.class);

    public static List<PostNews> crawNews(String proxyHost, int proxyPort) {
        LocalDate range = LocalDate.now().minusMonths(3);
        List<PostNews> postNewsList = Lists.newArrayList();
        for (String item : urls) {
            int page = 1;
            boolean inThreeMonth = true;
            while (inThreeMonth) {
                String url = item;
                if (page > 1) url = item + "page/" + page;
                log.info(url);
                Document search = null;
                try {
                    search = Jsoup.parse(RequestUtil.proxyGet(url, proxyHost, proxyPort, Maps.newHashMap()));
                } catch (IOException e) {
                    continue;
                }
                Elements newsList = search.getElementsByClass("td-block-span6");
                for (Element news : newsList) {
                    String time = JsoupUtil.getFirstElementByClass(news, "entry-date updated td-module-date", "datetime");
                    LocalDate localDate = TimeUtil.parseDate(time, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    if (localDate != null && range.isAfter(localDate)) {
                        inThreeMonth = false;
                        break;
                    }
                    if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                    String href = JsoupUtil.getFirstElementByClass(news, "td-image-wrap", "href");
                    String title = JsoupUtil.getFirstElementByClass(news, "td-image-wrap", "title");
                    String imgUrl = JsoupUtil.getFirstElementByClass(news, "entry-thumb", "src");
                    if (StringUtils.isBlank(href)) href = JsoupUtil.selectFirst(news,"a","href");
                    if (StringUtils.isBlank(title)) title = JsoupUtil.selectFirst(news,"a","title");
                    Document detail = null;
                    try {
                        detail = Jsoup.parse(RequestUtil.proxyGet(href, proxyHost, proxyPort, Maps.newHashMap()));
                    } catch (IOException e) {
                        continue;
                    }
                    String authorTxt = JsoupUtil.getFirstElementByClass(detail, "td-post-author-name", null);
                    String author = null;
                    if (StringUtils.isNotBlank(authorTxt)) author = authorTxt.replaceFirst("By", "").trim();
                    Element contentE = detail.getElementsByClass("td-post-content").first();
                    StringBuilder content = new StringBuilder();
                    if (contentE != null){
                        Elements pTags = contentE.select("p");
                        pTags.forEach(p->content.append(p.text()));
                    }
                    PostNews postNews = PostNews.builder()
                            .author(author).title(title)
                            .time(time).content(content.toString())
                            .language(PostNews.EN_LANGUAGE).imgUrl(imgUrl)
                            .url(href)
                            .build();
                    postNewsList.add(postNews);
                    log.info(postNews);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
                page++;
            }
        }
        return postNewsList;
    }

    public static void main(String[] args) {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\MacauBusiness.json",
                postNews);
    }
}
