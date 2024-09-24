package com.bda.common;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author: anran.ma
 * @created: 2024/9/24
 * @description:
 **/
public class JsoupUtil {
    public static String getFirstElementByClass(String tag, Document document) {
        Element element = document.getElementsByClass(tag).first();
        return element == null ? "" : element.text();
    }

    public static String selectFirst(String property, Document document) {
        Element element = document.selectFirst(property);
        return element == null ? "" : element.text();
    }
}
