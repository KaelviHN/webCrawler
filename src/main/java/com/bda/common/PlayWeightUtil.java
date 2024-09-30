package com.bda.common;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: anran.ma
 * @created: 2024/9/30
 * @description:
 **/
public class PlayWeightUtil {
    public static String parseElement(Page page, String id) {
        ElementHandle infoElement = page.querySelector(id);
        return infoElement != null ? infoElement.textContent() : "";
    }

    public static String parseAllElement(Page page, String id) {
        List<ElementHandle> infoElements = page.querySelectorAll(id);
        if (infoElements == null) return "";
        StringBuilder sb = new StringBuilder();
        infoElements.forEach(infoElement -> sb.append(infoElement.textContent()));
        return sb.toString();
    }

    public static String parseE(Page page, String id, String attribute) {
        // 选择包含链接的元素
        Locator link = page.locator(id);
        return link != null ? link.getAttribute(attribute) : "";
    }


}
