package com.bda.news;

import com.bda.common.PostNews;
import com.bda.common.RequestUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.util.UriEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/21
 * @description:
 **/
public class Sohu {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static List<PostNews> crawNews(String keyWord) {
        HashMap<String, Object> header = Maps.newHashMap();
        header.put("referer","https://search.sohu.com/?queryType=edit&keyword=%E6%BE%B3%E9%97%A8%E9%80%89%E4%B8%BE&spm=smpc.csrpage.0.0.17268960292452ZUoTCk");
        JsonNode searchJson = objectMapper.readTree( RequestUtil.commonGet("https://search.sohu.com/search/meta?keyword=" + UriEncoder.encode(keyWord)+"&terminalType=pc&spm-pre=smpc.csrpage.0.0.17268960292452ZUoTCk&SUV=1726895784391ce0om2&from=0&size=10&searchType=news&queryType=outside&queryId=17268960680005r6z006&pvId=17268960292452ZUoTCk&refer=https%253A%2F%2Fwww.sohu.com%2F&spm=smpc.csrpage.0.0.17268960292452ZUoTCk&maxL=15", header));
        s
        return Lists.newArrayList();

    }

    public static void main(String[] args) {
        System.out.println(crawNews("澳门选举"));
    }
}
