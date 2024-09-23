package com.bda.common;

import com.bda.WebCrawlerApplication;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author: anran.ma
 * @created: 2024/9/13
 * @description:
 **/


public class FileUtil {
    public static final String CONFIG_PATH = "config.json";
    public static final Log log = LogFactory.getLog(FileUtil.class);


    public static String getJarPath() {
        String jarPath = WebCrawlerApplication.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        jarPath = jarPath.replaceFirst("file:", "");
        return jarPath.substring(0, jarPath.indexOf("webCrawler.jar") - 1);
    }

    @SneakyThrows
    public static Config getConfig(String path) {
        return new ObjectMapper().readValue(new File(path + "/" + CONFIG_PATH), Config.class);
    }


    @SneakyThrows
    public static void cover(String path, Index index, String name) {
        String content = new ObjectMapper().writeValueAsString(index);
        String filePath = path + "/" + name;
        try (FileWriter fileWriter = new FileWriter(filePath, false)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("成功写入:" + filePath);
    }

    @SneakyThrows
    public static <T> void writeHistory(String path, List<T> indices, String name) {
        String content = new ObjectMapper().writeValueAsString(indices);
        String filePath = path + "\\" + name;
        try (FileWriter fileWriter = new FileWriter(filePath, false)) {
            fileWriter.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("成功写入:" + filePath);
    }

    @SneakyThrows
    public static void appendJsonToFile(String name, String path, Index index) {
        String filePath = path + "/" + name;
        ObjectMapper objectMapper = new ObjectMapper();
        List<Index> jsonList;

        // 检查文件是否存在
        if (Files.exists(Paths.get(filePath))) {
            // 如果文件存在，读取已有内容，并转换为 List<Index>
            jsonList = objectMapper.readValue(new File(filePath), new TypeReference<List<Index>>() {});
        } else {
            // 如果文件不存在，初始化为空数组
            jsonList = new ArrayList<>();
        }

        // 将新数据追加到数组
        jsonList.add(index);

        // 将更新后的数组写回文件，使用 distinct 去重
        objectMapper.writeValue(new File(filePath), jsonList.stream().distinct().collect(Collectors.toList()));
    }
}
