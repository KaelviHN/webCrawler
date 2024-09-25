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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author: anran.ma
 * @created: 2024/9/25
 * @description:
 **/
public class GcsGovMo {
    private static final Log log = LogFactory.getLog(GcsGovMo.class);
    private static final String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final Map<String, Object> header = Maps.newHashMap();

    static {
        header.put("wicket-ajax", "true");
        header.put("wicket-ajax-baseurl", "list/zh-hans/news/?0");
        header.put("x-requested-with", "XMLHttpRequest");
        header.put("referer", "https://www.gcs.gov.mo/news/list/zh-hans/news/?0");
    }

    public static List<PostNews> crawNews(String proxyHost, int proxyPort) {
        LocalDate now = LocalDate.now().minusMonths(3);
        boolean inThreeMonth = true;
        List<PostNews> postNewsList = Lists.newArrayList();
        String key = "";
        while (inThreeMonth) {
            Document search = null;
            try {
                if (StringUtils.isNotBlank(key)) {
                    String url = "https://www.gcs.gov.mo/news/list/zh-hans/news" + key.substring(1);
                    search = Jsoup.parse(RequestUtil.proxyGet(url, proxyHost, proxyPort, header));
                    log.info(url);
                } else {
                    // 获取首次页面
                    search = Jsoup.parse(RequestUtil.proxyGet("https://www.gcs.gov.mo/news/list/zh-hans/news/?0", proxyHost, proxyPort, Maps.newHashMap()));
                    // 获取下一页的key
                    key = JsoupUtil.getFirstElementByClass(search, "infiniteNext nextItems", "href");
                    log.info("https://www.gcs.gov.mo/news/list/zh-hans/news/?0");
                }
            } catch (IOException e) {
                continue;
            }
            Elements newsList = search.getElementsByClass("gridView hide-for-small-only");
            if (newsList.isEmpty()) {
                // 获取新闻列表
                Element ajaxResponse = search.select("ajax-response").first();
                if (ajaxResponse != null) {
                    // 获取 <component> 元素
                    Element component = ajaxResponse.select("component").first();
                    if (component != null) {
                        // 提取其 CDATA 内的内容
                        String innerHtml = component.ownText(); // 获取 CDATA 中的内容
                        // 解析内部 HTML
                        Document innerDocument = Jsoup.parse(innerHtml);
                        key = JsoupUtil.getFirstElementByClass(innerDocument, "infiniteNext nextItems", "href");
                        // 现在可以获取你需要的元素
                        newsList = innerDocument.getElementsByClass("gridView hide-for-small-only");
                    }
                }
            }
            for (Element element : newsList) {
                String href = JsoupUtil.selectFirst(element, "a[class=baseInfo container grid-x grid-margin-y]", "href");
                if (StringUtils.isBlank(href)) continue;
                href = href.split(";")[0].substring(href.lastIndexOf("..") == -1 ? 0 : href.lastIndexOf("..") + 2);
                if (StringUtils.isBlank(href)) continue;
                href = "https://www.gcs.gov.mo/news" + href;
                String imgUrl = JsoupUtil.getFirstElementByClass(element, "pin fullHeight jq_lazyloadimg", "data-src");
                if (StringUtils.isNotBlank(imgUrl))
                    imgUrl = imgUrl.substring(imgUrl.lastIndexOf("..") == -1 ? 0 : imgUrl.lastIndexOf("..") + 2);
                imgUrl = "https://www.gcs.gov.mo/news" + imgUrl;
                String title = JsoupUtil.getFirstElementByClass(element, "txt", null);
                String author = JsoupUtil.getFirstElementByClass(element, "auto cell line1Truncate dept", null);
                String time = JsoupUtil.getFirstElementByClass(element, "render_timeago_css not_render ", "datetime");
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null && now.isAfter(localDate)) {
                    inThreeMonth = false;
                    break;
                }
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                String content = "";
                try {
                    Document document = Jsoup.parse(RequestUtil.proxyGet(href, proxyHost, proxyPort, Maps.newHashMap()));
                    content = JsoupUtil.getFirstElementByClass(document, "cell baseContent baseSize text-justify content NEWS", null);
                } catch (IOException e) {
                    continue;
                }
                PostNews postNews = PostNews.builder()
                        .url(href).imgUrl(imgUrl)
                        .title(title).author(author)
                        .time(time).content(content)
                        .build();
                log.info(postNews);
                postNewsList.add(postNews);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    continue;
                }
            }
        }
        return postNewsList;
    }

    public static void main(String[] args) throws IOException {
        List<PostNews> postNews = crawNews("127.0.0.1", 7890);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\GcsGovMo\\GcsGovMo.json", postNews);
    }
}
