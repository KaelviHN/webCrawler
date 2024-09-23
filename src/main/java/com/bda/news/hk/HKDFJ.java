package com.bda.news.hk;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Calendar;

/**
 * @author: anran.ma
 * @created: 2024/9/23
 * @description: 香港律政司爬虫
 **/
public class HKDFJ {
    //download https://legalref.judiciary.hk/doc/judg/word/vetted/other/en/2018/CACC000142A_2018.docx
    public static void search(String proxyHost, int proxyPort) throws IOException {
        int year = Calendar.getInstance().get(Calendar.YEAR) - 1;
        while (year >= 2012) {
            String url = "https://www.doj.gov.hk/tc/archive/notable_criminal_" + year + ".html";
            Document searchList = Jsoup.parse(proxyGet(url, proxyHost, proxyPort));
            Elements elements = searchList.getElementsByClass("tblRow");
            for (Element element : elements) {
                String infoUrl = element.selectFirst("a").attr("href");
                Document content = Jsoup.parse(proxyGet(infoUrl, proxyHost, proxyPort));
                Document header = Jsoup.parse(proxyGet("https://legalref.judiciary.hk/lrs/common/search/" + content.selectFirst("frame[name=topFrame]").attr("src"), proxyHost, proxyPort));
                Elements aTags = header.select("a");
                for (Element aTag : aTags) {
                    if (aTag.text().equals("瀏覽word") && StringUtils.isNotBlank(aTag.attr("href")) && aTag.attr("href").endsWith(".docx")) {
                        String downloadUrl = "https://legalref.judiciary.hk/" + aTag.attr("href");
                        String[] split = downloadUrl.split("/");
                        String fileName = split[split.length - 1];
                        downloadFile(downloadUrl, "C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\HKDFJ\\document\\"+fileName, proxyHost, proxyPort);
                    }
                }
            }
            year--;
        }
    }

    public static String proxyGet(String url, String proxyHost, int proxyPort) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        // 创建 OkHttpClient 并设置代理
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();
        // 发送请求
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void downloadFile(String fileURL, String savePath, String proxyHost, int proxyPort) throws IOException {
        URL url = new URL(fileURL);
        // 创建代理
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        // 使用代理打开连接
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection(proxy);
        httpConn.setRequestMethod("GET");

        // 检查响应码
        int responseCode = httpConn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // 输入流
            InputStream inputStream = new BufferedInputStream(httpConn.getInputStream());
            FileOutputStream outputStream = new FileOutputStream(savePath);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();
            System.out.println("文件下载成功: " + savePath);
        } else {
            System.out.println("文件下载失败，响应码: " + responseCode);
        }

        httpConn.disconnect();
    }


    public static void main(String[] args) throws IOException {
        search("127.0.0.1", 7890);
    }
}
