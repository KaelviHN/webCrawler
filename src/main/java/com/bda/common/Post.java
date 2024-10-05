package com.bda.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: anran.ma
 * @created: 2024/9/30
 * @description:
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    private String author;
    private String title;
    private String content;
    private String time;
    private String url;
    private Integer commentsNums;
    private Integer like;
    private Integer unlike;
    private List<Comment> comments;
}
