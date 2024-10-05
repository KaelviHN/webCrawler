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

    public static void toEnd(Page page){
        int previousHeight = 0;
        while (true) {
            // 获取当前文档的高度
            page.evaluate("document.body.scrollHeight");
            int currentHeight;

            // 向下滚动
            page.mouse().wheel(0, 1000);
            page.waitForTimeout(2000); // 等待加载数据

            // 获取新的高度
            currentHeight = (int)page.evaluate("document.body.scrollHeight");

            // 如果高度没有变化，则停止滚动
            if (currentHeight == previousHeight) {
                break;
            }
            previousHeight = currentHeight;
        }
    }
}
