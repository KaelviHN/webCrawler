package com.bda.news;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author: anran.ma
 * @created: 2024/9/20
 * @description:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostNews {
    public static final String EN_LANGUAGE = "EN";
    public static final String CN_LANGUAGE = "CN";
    private String title;
    private String author;
    private String url;
    private String time;
    private String content;
    private String imgUrl;
    private String like;
    private String views;
    private String language;

    public static String patternTime(String timestamp) {
        ZonedDateTime dateTime = Instant.ofEpochMilli(Long.parseLong(timestamp)).atZone(ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}
