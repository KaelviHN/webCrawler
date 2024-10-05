package com.bda.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: anran.ma
 * @created: 2024/9/30
 * @description:
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private String comment;
    private String username;
    private String time;
}
