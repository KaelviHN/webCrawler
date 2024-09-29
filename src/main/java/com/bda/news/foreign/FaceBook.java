package com.bda.news.foreign;

import com.microsoft.playwright.*;
import lombok.SneakyThrows;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/29
 * @description:
 **/
public class FaceBook {
    @SneakyThrows
    public static void scraper(String account, String password) {
        try (Playwright wright = Playwright.create()){
            Browser browser = wright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            page.navigate("https://www.facebook.com/ronlam1981/");
            // 填写用户名和密码
            page.waitForSelector("input[name='email']");
            page.fill("input[name='email']", account);
            page.fill("input[name='pass']", password);
            page.click("button[type='submit']");
            // 等待页面响应
            page.waitForSelector("div[class='xieb3on']");
            System.out.println(page.querySelector("div[data-pagelet='ProfileTilesFeed_0']"));
            //获取信息
            ElementHandle infoPage = page.querySelector("div[data-pagelet='ProfileTilesFeed_0']");
            ElementHandle motto = infoPage.querySelector("span[class='x193iq5w xeuugli x13faqbe x1vvkbs x1xmvt09 x1lliihq x1s928wv xhkezso x1gmr53x x1cpjm7i x1fgarty x1943h6x xudqn12 x3x7a5m x6prxxf xvq8zen xo1l8bm xzsf02u']");
            if (motto != null) System.out.println("motto = " + motto.textContent());
            List<ElementHandle> infos = infoPage.querySelectorAll("span[class='x193iq5w xeuugli x13faqbe x1vvkbs x1xmvt09 x1lliihq x1s928wv xhkezso x1gmr53x x1cpjm7i x1fgarty x1943h6x xudqn12 x3x7a5m x6prxxf xvq8zen xo1l8bm xzsf02u x1yc453h']");
            infos.forEach(info -> System.out.println(info.textContent()));

        }
    }




    public static void main(String[] args) {
        scraper("AraneidaSword@gmail.com", "Mar.130118");
        /**
         * try (Playwright playwright = Playwright.create()) {
         *             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
         *             Page page = browser.newPage();
         *             page.navigate("https://example.com"); // 替换为目标网址
         *
         *             // 向下滑动以加载新数据
         *             for (int i = 0; i < 5; i++) { // 假设我们要滑动5次
         *                 // 模拟向下滚动
         *                 page.evaluate("window.scrollBy(0, window.innerHeight);");
         *
         *                 // 等待新内容加载
         *                 page.waitForTimeout(2000); // 等待2秒，视情况调整
         *             }
         *
         *             // 在这里可以处理页面加载后的数据
         *             System.out.println("已加载新数据");
         *
         *             browser.close();
         *         }
         */
    }
}
