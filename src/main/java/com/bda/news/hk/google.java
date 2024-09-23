package com.bda.news.hk;

import com.bda.common.PostNews;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.Lists;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author: anran.ma
 * @created: 2024/9/23
 * @description:
 **/
public class google {

    public static List<PostNews> crawNews(){
        return Lists.newArrayList();
    }

    public static String searchHk(){
        String url = "https://news.google.com/topics/CAAqJQgKIh9DQkFTRVFvSUwyMHZNRE5vTmpRU0JYcG9MVWhMS0FBUAE?hl=zh-HK&gl=HK&ceid=HK%3Azh-Hant";
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 7890));
        // 创建 OkHttpClient 并设置代理
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        // 发送请求
        Request request = new Request.Builder()
                .url("https://news.google.com/topics/CAAqJQgKIh9DQkFTRVFvSUwyMHZNRE5vTmpRU0JYcG9MVWhMS0FBUAE?hl=zh-HK&gl=HK&ceid=HK%3Azh-Hant")
                .header("User-Agent", "Mozilla/5.0")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String parse(){
        String hkSearch = searchHk();
        Document document = Jsoup.parse(hkSearch);
        Elements elements = document.getElementsByClass("jKHa4e");
        for (Element element : elements) {
//            element.get("a").
        }
        return "";
    }

    public static void main(String[] args) {
        crawNews();
    }
}
