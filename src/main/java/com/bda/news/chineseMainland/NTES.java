package com.bda.news.chineseMainland;

import com.bda.common.FileUtil;
import com.bda.common.PlayWeightUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.bda.common.RequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
@Log4j2
public class NTES {
    private final static String pattern = "yyyy-MM-dd HH:mm:ss";

    /**
     * LocalDate range = LocalDate.now().minusDays(3);
     * List<PostNews> res = Lists.newArrayList();
     * HashMap<String, Object> header = Maps.newHashMap();
     * header.put("cookie",  "_ntes_nuid=893e1a40854e1d0d3fd6e7e6c70348df; s_n_f_l_n3=0ae60b18cb83c1551727765314114;" +
     * " NTES_PC_IP=%E4%BD%9B%E5%B1%B1%7C%E5%B9%BF%E4%B8%9C; ne_analysis_trace_id=1727765327529; " +
     * "pgr_n_f_l_n3=0ae60b18cb83c15517277653275303767; _ntes_origin_from=; _antanalysis_s_id=1727765332252;" +
     * " vinfo_n_f_l_n3=0ae60b18cb83c155.1.0.1727765314114.0.1727765332373; W_HPTEXTLINK=old");
     * String infos = RequestUtil.commonGet("https://www.163.com/search?keyword=" + URLEncoder.encode(keyWord, String.valueOf(StandardCharsets.UTF_8)), header);
     * System.out.println("https://www.163.com/search?keyword=" + URLEncoder.encode(keyWord, String.valueOf(StandardCharsets.UTF_8)));
     * Document document = Jsoup.parse(infos);
     * Matcher matcher = Pattern.compile("(\\d+)").matcher(document.getElementsByClass("keyword_title").get(0).text());
     * Integer total = Integer.valueOf(matcher.find() ? matcher.group(1) : "0");
     * Elements newsList = document.getElementsByClass("keyword_new keyword_new_none ");
     * newsList.addAll(document.getElementsByClass("keyword_new keyword_new_simple "));
     * for (Element element : newsList) {
     * Element link = element.select("a").first();
     * if (link == null) continue;
     * String url = link.attr("href");
     * Element imgE = element.selectFirst("img");
     * String imgUrl = null;
     * if (imgE != null) imgUrl = imgE.attr("src");
     * String title = link.text();
     * Element authorTag = element.getElementsByClass("keyword_source").first();
     * String author = authorTag != null ? authorTag.text() : "";
     * String contentPage = RequestUtil.commonGet(url, header);
     * Document content = Jsoup.parse(contentPage);
     * String postBody = content.getElementsByClass("post_body").text();
     * String post_info = content.getElementsByClass("post_info").text();
     * String time = post_info.split("来源")[0].trim();
     * if (StringUtils.isNotBlank(time)) time = time.substring(0, time.length() - 1);
     * LocalDate localDate = TimeUtil.parseDate(time, pattern);
     * if (localDate != null && localDate.isBefore(range)) break;
     * if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
     * if (StringUtils.isBlank(title)) {
     * title = content.title();
     * }
     * if (StringUtils.isBlank(author)) {
     * author = content.getElementsByClass("post_wemedia_name").first() != null ?
     * content.getElementsByClass("post_wemedia_name").first().text() : "";
     * }
     * if (StringUtils.isBlank(time)) {
     * time = content.getElementById("ne_wrap") != null ? content.getElementById("ne_wrap").attr("data-publishtime") : "";
     * }
     * if (StringUtils.isBlank(time)) {
     * time = content.selectFirst("meta[property=article:published_time]") != null ?
     * content.selectFirst("meta[property=article:published_time]").text() : "";
     * }
     * if (StringUtils.isBlank(time)) {
     * time = content.getElementsByClass("ptime").first() != null ?
     * content.getElementsByClass("ptime").first().text() : "";
     * }
     * if (StringUtils.isBlank(author)) {
     * author = content.selectFirst("a[class=author]") != null ?
     * content.selectFirst("a[class=author]").text() : "";
     * }
     * PostNews postNews = PostNews.builder()
     * .title(title).url(url)
     * .author(author).time(time)
     * .content(postBody).imgUrl(imgUrl)
     * .language(PostNews.CN_LANGUAGE)
     * .build();
     * res.add(postNews);
     * Thread.sleep(50);
     * }
     * return res.stream().distinct().collect(Collectors.toList());
     * @param keyWords
     * @return
     */

    @SneakyThrows
    public static List<PostNews> crawNews(List<String> keyWords) {
        List<PostNews> res = Lists.newArrayList();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            for (String keyWord : keyWords) {
                Page page = context.newPage();
                page.navigate("https://www.163.com/search?keyword=" + URLEncoder.encode(keyWord, String.valueOf(StandardCharsets.UTF_8)));
                List<ElementHandle> infos = page.querySelectorAll("div.keyword_new.keyword_new_simple ");
                infos.addAll(page.querySelectorAll("div.keyword_new.keyword_new_none "));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                for (ElementHandle info : infos) {
//                    log.info(info.evaluate("el => el.outerHTML"));
                    String imgUrl = Optional.ofNullable(info.querySelector("img"))
                            .map(item->item.textContent().trim())
                            .orElse(null);
                    String url = Optional.ofNullable(info.querySelector("h3"))
                            .map(urlOut -> urlOut.querySelector("a"))
                            .map(item->item.getAttribute("href"))
                            .orElse(null);
                    if (url == null) continue;
                    log.info(url);
                    String title = Optional.ofNullable(info.querySelector("h3"))
                            .map(urlOut -> urlOut.querySelector("a"))
                            .map(item->item.textContent().trim())
                            .orElse(null);
                    String time = Optional.ofNullable(info.querySelector("div.keyword_time"))
                            .map(item->item.textContent().trim())
                            .orElse(null);
                    String author = Optional.ofNullable(info.querySelector("div.keyword_source"))
                            .map(item->item.textContent().trim())
                            .orElse(null);
                    Page contentPage = context.newPage();
                    contentPage.navigate(url);
                    contentPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
                    String content = Optional.ofNullable(contentPage.querySelector("div.post_body"))
                            .map(item->item.textContent().trim())
                            .orElse(null);
                    PostNews build = PostNews.builder()
                            .url(url).title(title)
                            .content(content).author(author)
                            .title(title).imgUrl(imgUrl)
                            .language(PostNews.CN_LANGUAGE).time(time)
                            .build();
                    res.add(build);
                    log.info(build);
                    contentPage.close();
                }
            }
            browser.close();
        }
        return res;
    }


    public static void main(String[] args) {
        List<String> keyWords = Lists.newArrayList("香港庆祝国庆节", "香港与大湾区发展", "香港人才引进与培养", "澳门回归25周年", "香港", "澳门");
        List<PostNews> postNews = crawNews(keyWords);
        FileUtil.write("C:\\Users\\arane\\Desktop\\webCrawler\\src\\main\\resources\\news\\resource\\" + NTES.class.getSimpleName() + ".json", postNews);
    }
}
