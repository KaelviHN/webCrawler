package com.bda.news.Macao;

import com.bda.common.FileUtil;
import com.bda.common.JsoupUtil;
import com.bda.common.RequestUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
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
 * @created: 2024/9/24
 * @description:澳门力报
 **/
public class Exmoo {
    private static final String timeFormat = "dd/MM/yyyy";
    private static final Log log = LogFactory.getLog(Exmoo.class);

    public static List<PostNews> crawNews(String proxyHost, int proxyPort) {
        LocalDate now = LocalDate.now().minusMonths(3);
        boolean inThreeMonth = true;
        List<PostNews> postNewsList = Lists.newArrayList();
        int page = 1;
        while (inThreeMonth) {
            String url = "https://www.exmoo.com/hot" + (page == 1 ? "" : "/p" + page);
            log.info("search page url : " + url);
            Document search = null;
            try {
                search = Jsoup.parse(RequestUtil.proxyGet(url, proxyHost, proxyPort, Maps.newHashMap()));
            } catch (IOException e) {
                continue;
            }
            // 读取热门列表
            Elements hotList = search.getElementsByClass("hot-list-item");
            for (Element element : hotList) {
                // 获取连接
                Element urlTag = element.selectFirst("a");
                if (urlTag == null) continue;
                String href = urlTag.attr("href");
                if (StringUtils.isBlank(href)) continue;
                Element imgTag = element.selectFirst("img");
                String imgUrl = imgTag != null ? imgTag.attr("data-original") : "";
                Document news = null;
                try {
                    news = Jsoup.parse(RequestUtil.proxyGet(href, proxyHost, proxyPort, Maps.newHashMap()));
                } catch (IOException e) {
                    continue;
                }
                String title = JsoupUtil.selectFirst(news, "meta[property=og:title]", "content");
                String time = JsoupUtil.getFirstElementByClass(news, "article-pub-date", null);
                if (StringUtils.isBlank(time)) continue;
                LocalDate localDate = TimeUtil.parseDate(time, timeFormat);
                if (localDate!=null && now.isAfter(localDate)) {
                    inThreeMonth = false;
                    break;
                }
                if (localDate!=null)  time = TimeUtil.parseTimeToCommonFormat(localDate);
                StringBuilder content = new StringBuilder();
                Elements elements = news.getElementsByClass("article-content-p");
                elements.forEach(word -> content.append(word.text()));
                PostNews postNews = PostNews.builder()
                        .author("澳门力报").time(time)
                        .title(title).imgUrl(imgUrl)
                        .content(content.toString()).url(href)
                        .language(PostNews.CN_LANGUAGE)
                        .build();
                postNewsList.add(postNews);
                log.info(postNews);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            page++;
        }
        return postNewsList;
    }


    public static void main(String[] args) {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\Exmoo.json",
                postNews);
    }
}
