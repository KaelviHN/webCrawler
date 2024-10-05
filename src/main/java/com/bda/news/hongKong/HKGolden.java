package com.bda.news.hongKong;

import com.bda.common.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microsoft.playwright.*;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.bda.common.PlayWeightUtil.toEnd;

/**
 * @author: anran.ma
 * @created: 2024/10/1
 * @description:
 **/
@Log4j2
public class HKGolden {
    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static List<Post> scraper(String host, int port, String type) {
        LocalDateTime range = LocalDateTime.now().minusMonths(3);
        List<Post> posts = new ArrayList<>();
        int idx = 1;
        boolean isEnd = false;
        while (!isEnd) {
            JsonNode tree = null;
            try {
                tree = mapper.readTree(
                        RequestUtil.proxyGet("https://api.hkgolden.com/v1/topics/" + type + "/" + idx + "?thumb=Y&sort=0&sensormode=N&filtermodeS=N&hideblock=N&limit=-1"
                                , host, port, Maps.newHashMap()));
                log.info("https://forum.hkgolden.com/channel/NW{}?thumb=Y&sort=0&sensormode=N&filtermodeS=N&hideblock=N&limit=-1", idx);
                idx++;
            } catch (IOException e) {
                continue;
            }
            Integer maxPage = JsonNodeUtil.parseElement(tree, Integer.class, "data", "maxPage");
            if (maxPage != null && maxPage < idx) isEnd = true;
            JsonNode postList = JsonNodeUtil.parseArray(tree, "data", "list");
            if (postList == null) break;
            for (JsonNode p : postList) {
                String id = p.get("id").asText();
                String title = p.get("title").asText();
                int page = 1;
                // 帖子链接
                if (id == null) continue;
                String url = "https://api.hkgolden.com/v1/view/" + id + "/1?sensormode=N&hideblock=N";
                String hrf = "https://forum.hkgolden.com/thread/" + id;
                // 发布信息
                JsonNode contentJson = null;
                try {
                    contentJson = mapper.readTree(RequestUtil.proxyGet(url, host, port, Maps.newHashMap()));
                    log.info(url);
                } catch (IOException e) {
                    continue;
                }
                String author = JsonNodeUtil.parseElement(contentJson, String.class, "data", "authorName");
                String content = JsonNodeUtil.parseElement(contentJson, String.class, "data", "content");
                if (content != null) content = Jsoup.parse(content).text().trim();
                Long timestamp = JsonNodeUtil.parseElement(contentJson, Long.class, "data", "messageDate");
                Integer like = JsonNodeUtil.parseElement(contentJson, Integer.class, "data", "marksGood");
                Integer unlike = JsonNodeUtil.parseElement(contentJson, Integer.class, "data", "marksBad");
                String time = null;
                if (timestamp != null) {
                    LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
                    if (date.isBefore(range)) {
                        isEnd = true;
                        break;
                    }
                    time = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }
                Post post = Post.builder()
                        .title(title).author(author).content(content).time(time).url(hrf).like(like).unlike(unlike)
                        .build();
                log.info(post);
                Integer totalPage = JsonNodeUtil.parseElement(contentJson, Integer.class, "data", "totalPage");
                if (totalPage == null) totalPage = 1;
                List<Comment> comments = Lists.newArrayList();
                while (page <= totalPage) {
                    String commentUrl = "https://api.hkgolden.com/v1/view/" + id + "/" + page + "?sensormode=N&hideblock=N";
                    // 发布信息
                    JsonNode commentJson = null;
                    try {
//                        Thread.sleep(10 * 1000);
                        commentJson = mapper.readTree(RequestUtil.proxyGet(commentUrl, host, port, Maps.newHashMap()));
                        log.info(commentUrl);
                    } catch (IOException e) {
                        continue;
                    }
                    JsonNode replies = JsonNodeUtil.parseArray(commentJson, "data", "replies");
                    if (replies == null) {
                        posts.add(post);
                        continue;
                    }
                    for (JsonNode reply : replies) {
                        String username = JsonNodeUtil.parseElement(reply, String.class, "authorName");
                        String comment = JsonNodeUtil.parseElement(reply, String.class, "content");
                        if (comment != null) {
                            Document document = Jsoup.parse(comment);
                            Elements blockquotes = document.getElementsByTag("blockquote");
                            Stack<String> stack = new Stack<>();
                            for (Element blockquote : blockquotes) {
                                // 删除内部 blockquote，以便只获取外部文本
                                blockquote.select("blockquote").remove();
                                String text = blockquote.text(); // 获取处理后的文本
                                if (!text.isEmpty()) stack.push(text);
                            }
                            document.getElementsByTag("blockquote").remove();
                            StringBuilder re = new StringBuilder();
                            stack.forEach(item -> re.append(item.trim()).append("\n").append("<==").append("\n"));
                            re.append(document.text());
                            comment = re.toString();
                        }
                        Long replyTimeStamp = JsonNodeUtil.parseElement(reply, Long.class, "replyDate");
                        String replyTime = null;
                        if (replyTimeStamp != null) {
                            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(replyTimeStamp), ZoneId.systemDefault());
                            replyTime = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        }
                        Comment build = Comment.builder().username(username).comment(comment).time(replyTime).build();
                        log.info(build);
                        comments.add(build);
                    }
                    page++;
                }
                post.setComments(comments);
                post.setCommentsNums(comments.size());
                posts.add(post);
            }

        }
        return posts;
    }

    public static void main(String[] args) {
        List<String> categories = Lists.newArrayList("BW", "HT", "CA", "ET", "SP", "FN", "ST",
                "SY", "EP", "SN", "JS", "HW", "IN", "SW", "MP", "AP", "BC", "AI",
                "ED", "TR", "CO", "AN", "TO", "MU", "VI", "DC", "TS", "WK", "LV", "SC",
                "BB", "PT", "HC", "MB", "RA", "AC", "BS", "JT");

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<Future<List<Post>>> futures = new ArrayList<>();
        // 提交任务
        for (String category : categories) {
            futures.add(executorService.submit(() -> scraper("127.0.0.1", 7890, category)));
        }
        // 收集结果
        List<Post> scrapers = new ArrayList<>();
        for (Future<List<Post>> future : futures) {
            try {
                scrapers.addAll(future.get());
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常
            }
        }
        // 去重和写入文件
        List<Post> posts = scrapers.stream().distinct().collect(Collectors.toList());
        FileUtil.write("C:\\Users\\arane\\Desktop\\webCrawler\\src\\main\\resources\\news\\resource\\" + HKGolden.class.getSimpleName() + ".json",
                posts);
        // 关闭线程池
        executorService.shutdown();
    }
}
