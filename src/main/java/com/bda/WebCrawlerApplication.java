package com.bda;


import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebCrawlerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(WebCrawlerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
