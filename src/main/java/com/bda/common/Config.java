package com.bda.common;

import lombok.Data;
import org.springframework.stereotype.Component;



/**
 * @author: anran.ma
 * @created: 2024/9/13
 * @description:
 **/
@Data
@Component
public class Config {
    private String host;
    private String username;
    private String password;
    private String path ;
    private Integer port;
    private String corn;
}
