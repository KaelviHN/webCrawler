package com.bda.common;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
public class JsonNodeUtil {
    /**
     * 解析json对应名称的信息
     * @param node
     * @param names
     * @return
     */
    public static String parseElement(JsonNode node, String... names) {
        node = parseNode(node, names);
        return node != null ? node.asText() : null;
    }

    /**
     * 解析节点
     * @param node
     * @param names
     * @return
     */
    public static JsonNode parseNode(JsonNode node, String... names) {
        for (String name : names) {
            node = node.path(name);
            if (node.isMissingNode()) return null;
        }
        return node.isMissingNode() ? null : node;
    }


    /**
     * 获取json对应路径的数组
     * @param node
     * @param names
     * @return
     */
    public static JsonNode parseArray(JsonNode node, String... names) {
        node = parseNode(node, names);
        return node == null || !node.isArray() ? null : node;
    }
}
