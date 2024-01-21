package com.cen.websky.pojo.dto;

import lombok.Data;

@Data
public class FileDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private Long userId;
}
