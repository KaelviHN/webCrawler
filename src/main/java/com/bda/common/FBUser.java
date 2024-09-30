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
@NoArgsConstructor
@AllArgsConstructor
public class FBUser {
    private String username;
    private String educate;
    private String job;
    private String motto;
    private String link;
    private List<FBUser> friends;
    private List<Post> posts;
}
