package com.cen.websky.pojo.vo;

import lombok.Data;

import java.net.URL;
import java.time.LocalDateTime;

@Data
public class FileVO {
    private String fileName;
    private URL url;
    private Long size;
    private LocalDateTime updateTime;
    private String category;
}
