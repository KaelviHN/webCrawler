package com.bda.news.foreign;


import com.bda.common.*;
import com.google.common.collect.Lists;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDate;
import java.util.List;
import static com.bda.common.PlayWeightUtil.*;
/**
 * @author: anran.ma
 * @created: 2024/9/30
 * @description:
 **/

@Log4j2
public class Facebook {
    private static final String fbHomeUrl = "https://mbasic.facebook.com";
    private static final String homeUrl = "https://www.facebook.com";
    private static final String fbUsername = "AraneidaSword@gmail.com";
    private static final String fbPassword = "Mar.130118";
    private static final String suffix = "?v=timeline";
    private static final String friend_suffix = "/friends";
    private static final String regex = "\"(\\\\d{4})年(\\\\d{1,2})月(\\\\d{1,2})日\"";
    private static final List<String> ids = Lists.newArrayList("ronlam1981",
            "LamUTou",
            "SynergyMacao",
            "hoinfong",
            "homanhomen.lee",
            "100033885754838",
            "josepereiracoutinho.macau",
            "Sugarlam1113",
            "100005159899356",
            "vengchai.leong",
            "kamsan.au",
            "diana.domar.5",
            "jose.estorninho",
            "chan.benny.5815",
            "allaboutmacau",
            "avachan330",
            "touian.sio",
            "kengtong.ian",
            "chan.benny.5815",
            "nicholas.cheong.313",
            "sonpou",
            "jose.m.encarnacao",
            "oclarimeng",
            "paulo.rego.98");

    @SneakyThrows
    public static void main(String[] args) {
        List<FBUser> fbUsers = Lists.newArrayList();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            fbInit(page);
            for (String id : ids) {
                page = context.newPage();
                page.navigate(fbHomeUrl + "/" + id + suffix);
                Thread.sleep(1000 * 10);
                FBUser fbUser = getUserInfo(page, id);
                List<Post> posts = Lists.newArrayList();
                log.info(fbUser);
                List<ElementHandle> postLinkElements = getPostLinks(page);
                postLinkElements.forEach(postLinkElement -> {
                    String link = postLinkElement.getAttribute("href");
                    log.info(link);
                    // 新开一个页面并导航到获取的链接
                    Page postPage = context.newPage();
                    postPage.navigate(fbHomeUrl + link);
                    Post post = parsePost(postPage, fbHomeUrl + link);
                    log.info(post);
                    posts.add(post);
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ignored) {
                    }
                });
                fbUser.setPosts(posts);
                fbUser.setFriends(getUserFriend(id, context));
                fbUsers.add(fbUser);
                page.close();
            }
            browser.close();
        }
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\" + Facebook.class.getSimpleName() + ".json",
                fbUsers);
    }

    private static void fbInit(Page page) {
        page.navigate(fbHomeUrl);
        loginFB(page);
    }

    private static void loginFB(Page page) {
        page.waitForSelector("input[name='email']");
        page.fill("input[name='email']", fbUsername);
        page.waitForSelector("input[name='pass']");
        page.fill("input[name='pass']", fbPassword);
        page.waitForSelector("button[type='submit']");
        page.click("button[type='submit']");
        page.waitForLoadState(LoadState.NETWORKIDLE); // 等待页面加载
    }

    public static FBUser getUserInfo(Page page, String id) {
        String username = page.title();
        String job = parseElement(page, "span.bz.mfss");
        String educate = parseAllElement(page, "div.bw.bx").replace(job, "");
        String motto = parseElement(page, "div[class^='_52ja'][class*='cc cd ce']");
        String link = homeUrl + "/" + id;
        return FBUser.builder().username(username).job(job).educate(educate).motto(motto).link(link).build();
    }

    public static List<ElementHandle> getPostLinks(Page page) {
        return page.locator("a[href*='story.php']").filter(new Locator.FilterOptions().setHasText("完整動態")).elementHandles();
    }

    @SneakyThrows
    public static Post parsePost(Page page, String url) {
        LocalDate range = LocalDate.now().minusMonths(3);
        List<Comment> comments = Lists.newArrayList();
        String content = parseElement(page, "div.bb");
        String time = page.locator("abbr").first().textContent();
        if (!time.contains("年")) time = LocalDate.now().getYear() + "年" + time;
        LocalDate localDate = TimeUtil.regexDate(time, regex);
        if (localDate != null && localDate.isBefore(range)) return null;
        if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
        List<ElementHandle> commentDivs = page.locator("div[class^='d']:not([class*=' '])[id]").elementHandles();
        commentDivs.forEach(commentDiv -> {
            // 第一个a标签
            ElementHandle usernameE = commentDiv.querySelector("a");
            ElementHandle commentE = commentDiv.querySelector("div[class]");
            String comment = commentE != null ? commentE.textContent() : null;
            String username = usernameE != null ? usernameE.textContent() : null;
            if (username!=null && username.equals(comment)) return;
            comments.add(Comment.builder()
                    .comment(comment)
                    .username(username)
                    .build());
        });
        Thread.sleep(1000 * 10);
        page.close();
        return Post.builder().content(content).time(time).url(url).comments(comments).build();
    }

    public static List<FBUser> getUserFriend(String id, BrowserContext context) {
        List<FBUser> friends = Lists.newArrayList();
        Page page = context.newPage();
        page.navigate(homeUrl + "/" + id + friend_suffix);
        int previousHeight = 0;
        while (true) {
            // 获取当前文档的高度
            page.evaluate("document.body.scrollHeight");
            int currentHeight;

            // 向下滚动
            page.mouse().wheel(0, 1000);
            page.waitForTimeout(2000); // 等待加载数据

            // 获取新的高度
            currentHeight = (int)page.evaluate("document.body.scrollHeight");

            // 如果高度没有变化，则停止滚动
            if (currentHeight == previousHeight) {
                break;
            }
            previousHeight = currentHeight;
        }
        List<ElementHandle> elementHandles = page.querySelectorAll("x1i10hfl xjbqb8w x1ejq31n xd10rxx x1sy0etr x17r0tee x972fbf xcfux6l x1qhh985 xm0m39n x9f619 x1ypdohk xt0psk2 xe8uvvx xdj266r x11i5rnm xat24cr x1mh8g0r xexx8yu x4uap5 x18d9i69 xkhd6sd x16tdsg8 x1hl2dhg xggy1nq x1a2a7pz x1heor9g x1sur9pj xkrqix3 x1s688f");
        elementHandles.forEach(elementHandle -> {
            String url = elementHandle.getAttribute("href");
            String username = elementHandle.textContent();
            friends.add(FBUser.builder().username(username).link(url).build());
        });
        return friends;
    }

}
