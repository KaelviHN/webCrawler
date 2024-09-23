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
    public static <T> T parseElement(JsonNode node, Class<T> clazz, String... names) {
        node = parseNode(node, names);
        if (node == null) {
            return null;
        }
        if (clazz.equals(Integer.class)) {
            return clazz.cast(node.asInt());
        } else if (clazz.equals(Long.class)) {
            return clazz.cast(node.asLong());
        } else if (clazz.equals(Double.class)) {
            return clazz.cast(node.asDouble());
        } else if (clazz.equals(Boolean.class)) {
            return clazz.cast(node.asBoolean());
        } else if (clazz.equals(String.class)) {
            return clazz.cast(node.asText());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
        }
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
