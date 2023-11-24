package com.cen.websky;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cen.websky.mapper")
public class WebskyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebskyBackendApplication.class, args);
    }

}
