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
    public static void scraper(String account, String password, List<String> urls) {
        String base = "https://www.facebook.com/login";
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();
            // 导航到登录页面
            page.navigate(base);
            // 填写用户名和密码
            page.waitForSelector("input[name='email']");
            page.fill("input[name='email']", account);
            page.fill("input[name='pass']", password);
            page.click("button[type='submit']");
            // 等待成功跳转
            page.waitForSelector("span[class='x9f619 x1ja2u2z x78zum5 x2lah0s x1n2onr6 x1qughib x1qjc9v5 xozqiw3 x1q0g3np x1pi30zi x1swvt13 xyamay9 xykv574 xbmpl8g x4cne27 xifccgj']");
            List<ElementHandle> elementHandles = page.querySelectorAll("span[class='x9f619 x1ja2u2z x78zum5 x2lah0s x1n2onr6 x1qughib x1qjc9v5 xozqiw3 x1q0g3np x1pi30zi x1swvt13 xyamay9 xykv574 xbmpl8g x4cne27 xifccgj']");
            for (int i = 0; i < Math.min(2, elementHandles.size()); i++) {
                List<ElementHandle> elements = page.querySelectorAll("div[class='x193iq5w xeuugli x13faqbe x1vvkbs x1xmvt09 x1lliihq x1s928wv xhkezso x1gmr53x x1cpjm7i x1fgarty x1943h6x xudqn12 x3x7a5m x6prxxf xvq8zen xo1l8bm xzsf02u x1yc453h']");
                elements.forEach(elementHandle -> System.out.println(elementHandle.textContent()));
            }
            browser.close();
        }
    }

    public static void scraperTest(String account, String password) {
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
            page.waitForSelector("div.x9f619.x1n2onr6.x1ja2u2z.xeuugli.xs83m0k.xjl7jj.x1xmf6yo.x1emribx.x1e56ztr.x1i64zmx.xnp8db0.x1d1medc.x7ep2pv.x1xzczws");
            //获取信息
            ElementHandle elementHandle = page.querySelector("div[class='x2b8uid x80vd3b x1q0q8m5 xso031l x1l90r2v']");
            browser.close();
        }
    }


    public static void main(String[] args) {
//        List<String> urls = Lists.newArrayList("https://www.facebook.com/ronlam1981/");
//        scraper(, urls);
        scraperTest("AraneidaSword@gmail.com", "Mar.130118");
    }
}
