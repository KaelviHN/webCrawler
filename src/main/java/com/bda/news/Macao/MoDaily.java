package com.bda.news.Macao;

import com.bda.common.JsonNodeUtil;
import com.bda.common.RequestUtil;
import com.bda.news.PostNews;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/25
 * @description:
 **/
public class MoDaily {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<PostNews> crawNews(String proxyHost, int proxyPort) {
        int page = 0;
        boolean inThreeMonth = true;
        List<PostNews> postNewsList = Lists.newArrayList();
        while (inThreeMonth) {
            String url = "https://app.modaily.cn/app_if/getArticles?columnId=102&page=" + page + "&lastFileId=9323197&jsoncallback=angular.callbacks._9";
            String search = RequestUtil.commonGet(url, Maps.newHashMap());
            JsonNode root;
            try {
                root = mapper.readTree(search);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if (root.isMissingNode()) continue;
            JsonNode jsonNode = JsonNodeUtil.parseArray(root, "list");
            if (jsonNode==null) continue;

        }
        return postNewsList;
    }

    public static void main(String[] args) {
        crawNews("127.0.0.1", 7890);
    }
}
