package com.cen.websky.pojo.dto;

import lombok.Data;

@Data
public class ShareFileDTO {
    private String title;
    private String[] files;
    private Boolean open;
}
