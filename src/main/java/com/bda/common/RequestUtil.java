package com.bda.common;

import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class RequestUtil {
    /**
     * 通用GET
     * @param address
     * @return
     */
    @SneakyThrows
    public static String commonGet(String address, Map<String, Object> header) {
        URL url = new URL(address);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        header.forEach((key, value) -> connection.setRequestProperty(key, String.valueOf(value)));
        // 设置请求头，模拟浏览器访问
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");
        return getResponse(connection);
    }

    /**
     * 通用POST
     * @param url
     * @param params
     * @return
     */
    @SneakyThrows
    public static String commonPost(String url, Map<String, Object> params,Map<String,Object> header) {
        // 获取连接
        URL urlObject = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
        // 请求方法
        connection.setRequestMethod("POST");
        // 设置请求头
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        // 设置请求头，模拟浏览器访问
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.102 Safari/537.36");

        for (Map.Entry<String, Object> entry : header.entrySet()) {
            connection.setRequestProperty(entry.getKey(), (String) entry.getValue());
        }
        connection.setDoOutput(true);
        // 构造表单数据
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8"));
        }
        // 写入请求体
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return getResponse(connection);
    }

    /**
     * 根据链接获取相应
     * @param connection
     * @return
     */
    @SneakyThrows
    public static String getResponse(HttpURLConnection connection) {
        StringBuilder response = new StringBuilder();
        // 获取响应编码
        String encoding = connection.getContentEncoding();
        InputStream inputStream = connection.getInputStream();
        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
}
