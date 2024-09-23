package com.bda.news.ChineseMainland;

import com.bda.common.FileUtil;
import com.bda.news.PostNews;
import com.bda.common.RequestUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class NTES {
    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        List<PostNews> res = Lists.newArrayList();
        HashMap<String, Object> header = Maps.newHashMap();
        header.put("cookie", "_ntes_nuid=38b3557a1fb96bb660641d0c4d7d2ccd;" +
                             " _ns=NS1.2.2066059838.1689327265;" +
                             " _ntes_nnid=38b3557a1fb96bb660641d0c4d7d2ccd,1720189575765;" +
                             " P_INFO=a1754709706@163.com|1726287294|0|unireg|00&99|null&null&null#gud&440600#10#0#0|&0||a1754709706@163.com; " +
                             "_ntes_origin_from=;" +
                             " _antanalysis_s_id=1726842060435;" +
                             " W_HPTEXTLINK=old;" +
                             " NTES_PC_IP=%E4%BD%9B%E5%B1%B1%7C%E5%B9%BF%E4%B8%9C;" +
                             " Hm_lvt_ec7594daf2d3af08adf8f4c74b7cbdbc=1726842062;" +
                             " Hm_lpvt_ec7594daf2d3af08adf8f4c74b7cbdbc=1726842062;" +
                             " HMACCOUNT=D88ED8A86A3A6BB3");
        String infos = RequestUtil.commonGet("https://www.163.com/search?keyword=" + URLEncoder.encode(keyWord, String.valueOf(StandardCharsets.UTF_8)), header);
        Document document = Jsoup.parse(infos);
        Matcher matcher = Pattern.compile("(\\d+)").matcher(document.getElementsByClass("keyword_title").get(0).text());
        Integer total = Integer.valueOf(matcher.find() ? matcher.group(1) : "0");
        Elements newsList = document.getElementsByClass("keyword_new keyword_new_none ");
        newsList.addAll(document.getElementsByClass("keyword_img"));
        for (Element element : newsList) {
            Element link = element.select("a").first();
            if (link == null) continue;
            String url = link.attr("href");
            String title = link.text();
            Element authorTag = element.getElementsByClass("keyword_source").first();
            String author = authorTag != null ? authorTag.text() : "";
            String contentPage = RequestUtil.commonGet(url, header);
            Document content = Jsoup.parse(contentPage);
            String postBody = content.getElementsByClass("post_body").text();
            String post_info = content.getElementsByClass("post_info").text();
            String time = post_info.split("来源")[0].trim();
            if (StringUtils.isBlank(title)) {
                title = content.title();
            }
            if (StringUtils.isBlank(author)) {
                author = content.getElementsByClass("post_wemedia_name").first() != null ?
                        content.getElementsByClass("post_wemedia_name").first().text() : "";
            }
            if (StringUtils.isBlank(time)){
                time = content.getElementById("ne_wrap")!=null?content.getElementById("ne_wrap").attr("data-publishtime"):"";
            }
            if (StringUtils.isBlank(time)){
                time = content.selectFirst("meta[property=article:published_time]") !=null ?
                        content.selectFirst("meta[property=article:published_time]").text():"";
            }
            if (StringUtils.isBlank(time)){
                time = content.getElementsByClass("ptime").first() !=null ?
                        content.getElementsByClass("ptime").first().text():"";
            }
            if (StringUtils.isBlank(author)){
                author = content.selectFirst("a[class=author]") !=null ?
                        content.selectFirst("a[class=author]").text():"";
            }
            PostNews postNews = PostNews.builder()
                    .title(title).url(url)
                    .author(author).time(time)
                    .content(postBody)
                    .build();
            res.add(postNews);
            Thread.sleep(50);
        }
        return res.stream().distinct().collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<PostNews> postNewsList = crawNews("澳门");
        postNewsList.addAll(crawNews("香港"));
        FileUtil.writeHistory("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\NTES", postNewsList, "NTES.json");
    }
}
