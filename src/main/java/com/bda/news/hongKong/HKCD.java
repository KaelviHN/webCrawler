package com.bda.news.hongKong;

import com.bda.common.FileUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.google.common.collect.Lists;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.springframework.ui.context.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author: anran.ma
 * @created: 2024/10/2
 * @description:
 **/
public class HKCD {
    private static final Logger log = LogManager.getLogger(HKCD.class);

    public static List<PostNews> crawNews(List<String> categories) {
        List<PostNews> newsList = new ArrayList<>();
        LocalDate range = LocalDate.now().minusMonths(3);
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            categories.forEach(category -> {
                Page page = context.newPage();
                page.setDefaultNavigationTimeout(1000 * 60 * 3);
                page.navigate("https://www.hkcd.com.hk/hkcdweb/column/" + category + ".html");
                while (true) {
                    List<PostNews> news = getNews(page, context);
                    if (news.isEmpty()) break;
                    LocalDate date = TimeUtil.parseDate(news.get(news.size() - 1).getTime(), "yyyy-MM-dd");
                    if (date != null && date.isBefore(range)) break;
                    newsList.addAll(news);
                    page.click("div.item.next");
                    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                }
                page.close();
            });
            browser.close();
        }
        return newsList;
    }


    public static List<PostNews> getNews(Page page, BrowserContext context) {
        List<PostNews> newsList = new ArrayList<>();
        ElementHandle elementHandle = page.querySelector("div.list");
        if (elementHandle == null) return newsList;
        List<ElementHandle> handles = elementHandle.querySelectorAll("a[class='newItem ']");
        for (ElementHandle handle : handles) {
            String url = Optional.ofNullable(handle)
                    .map(item -> item.getAttribute("href"))
                    .orElse(null);
            if (url == null) continue;
            String imgUrl = Optional.ofNullable(handle.querySelector("img"))
                    .map(item -> item.getAttribute("data-src"))
                    .map(item -> "https://www.hkcd.com.hk" + item)
                    .orElse(null);
            String time = Optional.ofNullable(handle.querySelector("span.date"))
                    .map(ElementHandle::textContent)
                    .orElse(null);
            Page contentPage = context.newPage();
            contentPage.setDefaultNavigationTimeout(1000 * 60 * 10);
            try {
                contentPage.navigate(url);
            } catch (Exception e) {
                continue;
            }
            contentPage.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String title = contentPage.title();
            String content = Optional.ofNullable(contentPage.querySelector("div.newsDetail"))
                    .map(ElementHandle::textContent)
                    .orElse(null);
            if (content!=null) content = Jsoup.parse(content).text();
            String author = Optional.ofNullable(contentPage.querySelector("div.end"))
                    .map(ElementHandle::textContent)
                    .map(item -> {
                        String[] split = item.split("：");
                        if (split.length == 2) return split[1].trim();
                        return item.trim();
                    })
                    .orElse("香港商報網");
            PostNews build = PostNews.builder()
                    .title(title).content(content).time(time)
                    .author(author).imgUrl(imgUrl).language(PostNews.CN_LANGUAGE)
                    .url(url)
                    .build();
            contentPage.close();
            newsList.add(build);
            log.info(build);
        }
        return newsList;
    }

    public static void main(String[] args) {
        List<PostNews> postNews = crawNews(Lists.newArrayList("wq","gw"));
        FileUtil.write("C:\\Users\\arane\\Desktop\\webCrawler\\src\\main\\resources\\news\\resource\\"
                       + HKCD.class.getSimpleName() + ".json",
                postNews);
    }
}
