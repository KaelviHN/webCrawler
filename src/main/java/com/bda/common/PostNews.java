package com.bda.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String title;
    private String author;
    private String url;
    private String time;
    private String content;
    private String like;
}
