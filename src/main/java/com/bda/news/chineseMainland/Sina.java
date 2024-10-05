package com.bda.news.chineseMainland;

import com.bda.common.FileUtil;
import com.bda.common.JsoupUtil;
import com.bda.common.TimeUtil;
import com.bda.news.PostNews;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
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
    private static final String pattern = "yyyy年MM月dd日 HH:mm";
    private static final String patternCommon = "yyyy-MM-dd HH:mm:ss";
    private static final Logger log = LogManager.getLogger(Sina.class);

    public static List<PostNews> crawNews(String keyWord) {
        LocalDate range = LocalDate.now().minusDays(3);
        List<PostNews> postNewsList = Lists.newArrayList();
        Map<String, Object> payload = Maps.newHashMap();
        Map<String, Object> header = Maps.newHashMap();
        payload.put("q", keyWord);
        payload.put("c", "news");
        payload.put("adv", 0);
        payload.put("size", 10);
        payload.put("sort", "time");
        payload.put("page", 0);
        boolean inThreeMonth = true;
        header.put("Cookie", "UOR=,k.sina.com.cn,; ULV=1727717929648:1:1:1::; mYSeArcH=%u6FB3%u95E8; SGUID=1727719631320_45655150; SUB=_2A25L_pxFDeRhGeFJ7FQW9inMyT2IHXVpdZGNrDV_PUNbm9AbLWf4kW9NfzXLvHblQtaYSMj2o5e0amvoWuZ-CByI; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhDJ54ua9E8OJSSsXLpwi7u5JpX5KzhUgL.FoMNS0qNSoM7eo22dJLoI7yT9g8jUJLQKntt; ALF=1730312469; U_TRS1=00000085.c81a28606.66faec16.dae207f1; U_TRS2=00000085.c82128606.66faec16.0000353b; SessionID=0vk73og0u03qhbson6mv4uqe91; beegosessionID=34f6b006f78751e0ddfe5a28a653c6c8");
        while ((int) payload.get("page") <= 15 && inThreeMonth) {
            Document search = Jsoup.parse(RequestUtil.commonPost("https://search.sina.com.cn/news", payload, header));
            Elements elementsList = search.getElementsByClass("box-result clearfix");
            if (elementsList.isEmpty()) break;
            for (Element element : elementsList) {
                String imgUrl = JsoupUtil.selectFirst(element, "img", "src");
                Element info = element.select("a").first();
                if (info == null) continue;
                String url = info.attr("href");
                String title = info.text();
                Document mainDocument = Jsoup.parse(RequestUtil.commonGet(url, header));
                // 查找 name 属性为 "mediaid" 的 meta 标签
                String author = JsoupUtil.selectFirst(mainDocument, "meta[name=mediaid]", "content");
                if (StringUtils.isBlank(author))
                    author = JsoupUtil.selectFirst(mainDocument, "meta[property=article:author]", "content");
                if (StringUtils.isBlank(author))
                    author = JsoupUtil.getFirstElementByClass(mainDocument, "source ent-source", null);
                if (StringUtils.isBlank(author)) {
                    author = JsoupUtil.getFirstElementByClass(mainDocument, "from", null);
                    if (StringUtils.isNotBlank(author)) {
                        String[] split = author.split("来源：");
                        if (split.length > 1) author = split[1];
                    }
                }
                String time = JsoupUtil.getFirstElementByClass(mainDocument,"date",null);
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (StringUtils.isBlank(time) || localDate == null) {
                    Element from = mainDocument.getElementsByClass("from").first();
                    if (from != null){
                        Element em = from.getElementsByTag("em").first();
                        if (em != null){
                            localDate = TimeUtil.parseDate(time, patternCommon);
                        }
                    }

                }
                if (localDate != null && localDate.isBefore(range)) {
                    inThreeMonth = false;
                    break;
                }
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                Element articleTag = mainDocument.getElementById("article");
                String article = null;
                if (articleTag != null) article = articleTag.text();
                PostNews postNews = PostNews.builder()
                        .url(url).title(title)
                        .content(article).author(author)
                        .time(time).language(PostNews.CN_LANGUAGE)
                        .imgUrl(imgUrl)
                        .build();
                postNewsList.add(postNews);
                log.info(postNews);
                try {
                    Thread.sleep(5000+(long)(Math.random() * 500));
                } catch (InterruptedException ignored) {
                }
            }
            payload.put("page", (int) payload.get("page") + 1);
        }
        return postNewsList.stream().distinct().collect(Collectors.toList());
    }

    public static List<PostNews> crawNewsByList(List<String> keyWords) {
        List<PostNews> postNewsList = Lists.newArrayList();
        keyWords.forEach(keyWord -> postNewsList.addAll(crawNews(keyWord)));
        return postNewsList;
    }

    public static void main(String[] args) {
        List<String> keyWords = Lists.newArrayList( "香港庆祝国庆节","香港与大湾区发展", "香港人才引进与培养", "澳门回归25周年", "香港", "澳门");
        List<PostNews> postNews = crawNewsByList(keyWords);
        FileUtil.write("C:\\Users\\arane\\Desktop\\webCrawler\\src\\main\\resources\\news\\resource\\" + Sina.class.getSimpleName() + ".json", postNews);
    }
}
