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
    public static String getFirstElementByClass(Element e, String name, String attr) {
        Element element = e.getElementsByClass(name).first();
        return element == null ? null : attr == null ? element.text() : element.attr(attr);
    }

    public static String selectFirst(Element e, String property, String attr) {
        Element element = e.selectFirst(property);
        return element == null ? null : attr == null ? element.text() : element.attr(attr);
    }
}
