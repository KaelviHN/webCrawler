package com.bda.news.hongKong;

import com.bda.common.RequestUtil;
import com.google.common.collect.Maps;
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
    public static void search(String proxyHost, int proxyPort, String path) throws IOException {
        int year = Calendar.getInstance().get(Calendar.YEAR) - 1;
        while (year >= 2012) {
            String url = "https://www.doj.gov.hk/tc/archive/notable_criminal_" + year + ".html";
            Document searchList = Jsoup.parse(RequestUtil.proxyGet(url, proxyHost, proxyPort, Maps.newHashMap()));
            Elements elements;
            try {
                elements = searchList.getElementsByClass("tblRow");
            } catch (Exception e) {
                System.out.println("e = " + e + "url = " + url);
                continue;
            }
            for (Element element : elements) {
                Element at = element.selectFirst("a");
                if (at == null) continue;
                String infoUrl = at.attr("href");
                Document content = null;
                Document header = null;
                try {
                    content = Jsoup.parse(RequestUtil.proxyGet(infoUrl, proxyHost, proxyPort, Maps.newHashMap()));
                    Element top = content.selectFirst("frame[name=topFrame]");
                    if (top == null) continue;
                    header = Jsoup.parse(RequestUtil.proxyGet("https://legalref.judiciary.hk/lrs/common/search/" + top.attr("src"), proxyHost, proxyPort, Maps.newHashMap()));
                } catch (IOException e) {
                    continue;
                }
                Elements aTags = header.select("a");
                for (Element aTag : aTags) {
                    if (aTag.text().equals("瀏覽word") && StringUtils.isNotBlank(aTag.attr("href")) && aTag.attr("href").endsWith(".docx")) {
                        String downloadUrl = "https://legalref.judiciary.hk/" + aTag.attr("href");
                        String[] split = downloadUrl.split("/");
                        String fileName = split[split.length - 1];
                        try {
                            downloadFile(downloadUrl, path + fileName, proxyHost, proxyPort);
                        } catch (IOException e) {
                            continue;
                        }
                    }
                }
            }
            year--;
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
        }

        httpConn.disconnect();
    }


    public static void main(String[] args) throws IOException {
        search("127.0.0.1", 7890,"C:\\Users\\moon9\\Desktop\\webCrawler\\src\\main\\resources\\news\\HKDFJ\\docx\\");
    }
}
