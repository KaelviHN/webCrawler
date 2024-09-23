package com.bda.news.ChineseMainland;

import com.bda.common.FileUtil;
import com.bda.news.PostNews;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: anran.ma
 * @created: 2024/9/23
 * @description:
 **/
public class Sina {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        List<PostNews> postNewsList = Lists.newArrayList();
        Map<String, Object> payload = Maps.newHashMap();
        Map<String, Object> header = Maps.newHashMap();
        payload.put("q", keyWord);
        payload.put("c", "news");
        payload.put("adv", 0);
        payload.put("size", 100);
        payload.put("sort", "time");
        payload.put("page", 0);
        header.put("Cookie","UOR=www.bing.com,finance.sina.com.cn,; ULV=1726639779905:1:1:1::; U_TRS1=0000009a.8931452.66ea6ea3.a3ed98f1; SGUID=1726815326242_16180035; ALF=02_1729667050; SCF=AiVjgRl0_B3Qc_txlyOSSNnvJkDVW0IyqFFweBM15E1vfuV-Htv10URsOV4qBJ34sos4O6-mWN_TzLAmqod3W2Q.; SUB=_2A25L9WK6DeRhGeFJ7FQW9inMyT2IHXVoi_pyrDV_PUJbkNAbLRTWkW9NfzXLvJ2dXg76F1tWXy__HW_TjyqefNOv; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhDJ54ua9E8OJSSsXLpwi7u5NHD95QNS0McS0qNehzpWs4DqcjzCcHydG9DI2Rt; U_TRS2=00000045.d9c813720.66f112f1.2b17d43f; beegosessionID=972b930105ac75de00625487885304d5; mYSeArcH=%u6FB3%u95E8%u9009%u4E3E%7CsEaRchHIS%7C%u6FB3%u95E8%7CsEaRchHIS%7C%u9999%u6E2F%7CsEaRchHIS%7Caa");
        while ((int) payload.get("page") <= 15) {
            Document search = Jsoup.parse(RequestUtil.commonPost("https://search.sina.com.cn/news", payload,header));
            Elements elementsList = search.getElementsByClass("box-result clearfix");
            if (elementsList.isEmpty()) break;
            for (Element element : elementsList) {
                Element info = element.select("a").first();
                if (info == null) continue;
                String url = info.attr("href");
                String title = info.text();
                Document mainDocument = Jsoup.parse(RequestUtil.commonGet(url, header));
                // 查找 name 属性为 "mediaid" 的 meta 标签
                Element authorTag = mainDocument.selectFirst("meta[name=mediaid]");
                String author = "";
                if (authorTag != null) author = authorTag.text();
                Element dateTag = mainDocument.getElementsByClass("date").first();
                String time = "";
                if (dateTag != null) time = dateTag.text();
                Element articleTag = mainDocument.getElementById("article");
                String article = "";
                if (articleTag != null) article = articleTag.text();
                PostNews postNews = PostNews.builder()
                        .url(url).title(title)
                        .content(article).author(author)
                        .time(time)
                        .build();
                postNewsList.add(postNews);
                Thread.sleep(5000);
            }
            payload.put("page", (int) payload.get("page") + 1);

        }
        return postNewsList.stream().distinct().collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<PostNews> postNewsList = crawNews("澳门");
        postNewsList.addAll(crawNews("香港"));
        FileUtil.writeHistory("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\Sina",postNewsList,"Sina.json");
    }
}
