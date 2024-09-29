package com.bda.news.chineseMainland;

import com.bda.common.*;
import com.bda.news.PostNews;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.*;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class Tencent {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static String pattern = "yyyy-MM-dd HH:mm:ss";
    private static final Logger log = LogManager.getLogger(Tencent.class);

    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        // 请求体数据
        Map<String, Object> formData = Maps.newHashMap();
        Map<String, Object> header = Maps.newHashMap();
        formData.put("page", 0);
        formData.put("query", keyWord);
        formData.put("is_pc", 1);
        formData.put("hippy_custom_version", 24);
        formData.put("search_type", "all");
        formData.put("search_count_limit", 10);
        formData.put("appver", "15.5_qqnews_7.1.80");
        formData.put("suid", "8QIf3n5f7YQauj/Q5As=");
        header.put("Cookie", "RK=/3O8wQ8jzb; ptcz=8823d683d501dff9e7ef4902f4b8696aa8a5430e67c159d1e1d1fd8214442b14; pgv_pvid=2510486800; fqm_pvqid=3ff38aa7-2f69-4e50-a422-09bfe4218ea8; eas_sid=R1D7S2O4r2j9V4s7K0f7u9q0B9; _qimei_uuid42=189140a2e131004965608f24a1530ec4594c3ba859; _qimei_fingerprint=85e32e7409ab508386db29a23efb30b8; _qimei_q36=; _qimei_h38=2db8275c65608f24a1530ec40200000ca18914; wap_wx_openid=o_lAF51keIiU5bB8iquZPb_0f5cI; wap_wx_appid=wx0b6d22ad9f2c4fa0; logintype=1; wap_refresh_token=84_jXo4L_11U0ZRURP4UVZzmSsRsCMZHhA5tQVG54sgMezKvIIU4OXj5_0Asl0IOr_F7cLPWDNXKGityQARXSJKbvGwi1WkdZnbs9OiBGyOyts; wap_encrypt_logininfo=ASuZHXPxJsxaHE13GyDl4zL4FQ6zye87Vv34Pd%2Bul%2BE9Ee%2B4iO0DgQXSXyk%2FD5VpgbbKVEQRf%2FEfFs0qH8NKWjTezrW7jBs0U5x2aUJp%2BsOm; pac_uid=8QIf3n5f7YQauj/Q5As=; backup_logintype=1; news_refresh_token=84_jXo4L_11U0ZRURP4UVZzmSsRsCMZHhA5tQVG54sgMezKvIIU4OXj5_0Asl0IOr_F7cLPWDNXKGityQARXSJKbvGwi1WkdZnbs9OiBGyOyts; news_vuserid=1199227745; news_appid=wx0b6d22ad9f2c4fa0; news_openid=o_lAF51keIiU5bB8iquZPb_0f5cI; news_main_login=wx; suid=user_8QIf3n5f7YQauj%2FQ5As%3D; current-city-name=gz; refresh_token=84_jXo4L_11U0ZRURP4UVZzmSsRsCMZHhA5tQVG54sgMezKvIIU4OXj5_0Asl0IOr_F7cLPWDNXKGityQARXSJKbvGwi1WkdZnbs9OiBGyOyts; vuserid=1199227745; appid=wx0b6d22ad9f2c4fa0; openid=o_lAF51keIiU5bB8iquZPb_0f5cI; main_login=wx; news_token=EoABxS_KcsYRN0OuozgEczbFuKLtuRtVGmBz9g5paJ8NTzgO6hTOhVH6fchmKKr6UKeEYVkso8jo5VyIIYC8aATwyPw_VSqBvUwrneVHlYgo_VA5zTjDmoCWW8SmK7fSTBFPvJx4Nr4FZDw54FhejWH0eAwsf70KV0-xSAGXPk3ayf0gEQ; backup_news_token=EoABxS_KcsYRN0OuozgEczbFuKLtuRtVGmBz9g5paJ8NTzgO6hTOhVH6fchmKKr6UKeEYVkso8jo5VyIIYC8aATwyPw_VSqBvUwrneVHlYgo_VA5zTjDmoCWW8SmK7fSTBFPvJx4Nr4FZDw54FhejWH0eAwsf70KV0-xSAGXPk3ayf0gEQ; wap_wx_access_token=84_e05U-LuPuyBz8NiPZCDo5ZX-Ai0zldTfj4ayIYEn3XoGfn-tt1ZXxfGB-pbKscw8k0rI-eMRzkuP4P03tyj6zg81dAf4iPpnkS3_K58200A; news_vusession=4Y2VCvrJ6tRYabO7U5vvLA.N; news_access_token=84_e05U-LuPuyBz8NiPZCDo5ZX-Ai0zldTfj4ayIYEn3XoGfn-tt1ZXxfGB-pbKscw8k0rI-eMRzkuP4P03tyj6zg81dAf4iPpnkS3_K58200A; vusession=4Y2VCvrJ6tRYabO7U5vvLA.N; access_token=84_e05U-LuPuyBz8NiPZCDo5ZX-Ai0zldTfj4ayIYEn3XoGfn-tt1ZXxfGB-pbKscw8k0rI-eMRzkuP4P03tyj6zg81dAf4iPpnkS3_K58200A; lcad_o_minduid=MqvwvTI47F0MwoQIdkwis7nbyc_siNXO; lcad_appuser=7EFBB6A191FD75A5; lcad_Lturn=435; lcad_LKBturn=226; lcad_LPVLturn=567; lcad_LPLFturn=897; lcad_LPSJturn=883; lcad_LBSturn=605; lcad_LVINturn=385; lcad_LDERturn=179");
        String bsUrl = "https://i.news.qq.com/gw/pc_search/result";
        String infos = RequestUtil.commonPost(bsUrl, formData, Maps.newHashMap());
        // 获取搜索列新闻信息
        JsonNode tree = objectMapper.readTree(infos);
        List<PostNews> res = parse(tree);
        Long totalNum = JsonNodeUtil.parseElement(tree, Long.class, "total_num");
        while (totalNum != null && 10L * ((int) formData.get("page") + 1) < totalNum) {
            Thread.sleep(500);
            formData.put("page", (int) formData.get("page") + 1);
            infos = RequestUtil.commonPost(bsUrl, formData, Maps.newHashMap());
            // 获取搜索列新闻信息
            tree = objectMapper.readTree(infos);
            res.addAll(parse(tree));
        }
        return res;
    }

    @SneakyThrows
    public static List<PostNews> parse(JsonNode tree) {
        List<PostNews> postNewsList = Lists.newArrayList();
        JsonNode secList = JsonNodeUtil.parseNode(tree, "secList");
        if (secList == null) return Lists.newArrayList();
        Iterator<JsonNode> elements = secList.elements();
        while (elements.hasNext()) {
            JsonNode element = elements.next();
            // 解析基本本信息
            JsonNode newsList = JsonNodeUtil.parseArray(element, "newsList");
            if (newsList == null) continue;
            Iterator<JsonNode> newsIt = newsList.elements();
            while (newsIt.hasNext()) {
                JsonNode news = newsIt.next();
                String title = JsonNodeUtil.parseElement(news, String.class, "title");
                String time = JsonNodeUtil.parseElement(news, String.class, "time");
                LocalDate localDate = TimeUtil.parseDate(time, pattern);
                if (localDate != null) time = TimeUtil.parseTimeToCommonFormat(localDate);
                String url = JsonNodeUtil.parseElement(news, String.class, "url");
                String author = JsonNodeUtil.parseElement(news, String.class, "card", "chlname");
                String shareUrl = JsonNodeUtil.parseElement(news, String.class, "shareUrl");
                JsonNode imgArray = JsonNodeUtil.parseArray(news, "bigImage");
                String imgUrl = null;
                if (imgArray!=null && !imgArray.isEmpty()) imgUrl = imgArray.get(0).textValue();
                String content = parseNewsContent(shareUrl);
                PostNews postNews = PostNews.builder()
                        .title(title).time(time)
                        .author(author).url(url)
                        .content(content).imgUrl(imgUrl)
                        .language(PostNews.CN_LANGUAGE)
                        .build();
                log.info(postNews);
                postNewsList.add(postNews);
                Thread.sleep(5000);
            }
        }
        return postNewsList;
    }

    @SneakyThrows
    public static String parseNewsContent(String url) {
        String content = "";
        String html = RequestUtil.commonGet(url, Maps.newHashMap());
        Element head = Jsoup.parse(html).head();
        Elements scripts = head.select("script");
        for (Element script : scripts) {
            String scriptContent = script.html().trim();
            // 判断是否包含"window.DATA ="，以此作为定位JSON对象的依据
            if (scriptContent.contains("window.DATA")) {
                // 提取JSON字符串部分（去掉window.DATA等前缀）
                int jsonStartIndex = scriptContent.indexOf('{');
                int jsonEndIndex = scriptContent.lastIndexOf('}') + 1;

                if (jsonStartIndex != -1) {
                    String jsonString = scriptContent.substring(jsonStartIndex, jsonEndIndex);
                    // 解析JSON
                    JsonNode jsonNode = objectMapper.readTree(jsonString);
                    // 获取originContent中的text字段
                    String jsonText = JsonNodeUtil.parseElement(jsonNode, String.class, "originContent", "text");
                    if (jsonText != null) {
                        content = Jsoup.parse(jsonText).text();
                    }
                }
            }
        }
        return content;
    }


    public static List<PostNews> crawNewsByList(List<String> keyWords) {
        List<PostNews> postNewsList = Lists.newArrayList();
        keyWords.forEach(keyWord -> postNewsList.addAll(crawNews(keyWord)));
        return postNewsList;
    }

    public static void main(String[] args) {
        List<String> keyWords = Lists.newArrayList("香港庆祝国庆节", "香港与大湾区发展", "香港人才引进与培养", "澳门回归25周年", "香港", "澳门");
        List<PostNews> postNews = crawNewsByList(keyWords);
        FileUtil.write("C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\source\\" + Tencent.class.getSimpleName() + ".json", postNews);
    }

}
