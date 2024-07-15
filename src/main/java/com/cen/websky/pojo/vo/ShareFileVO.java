package com.cen.websky.pojo.vo;

import lombok.Data;

import java.util.List;

@Data
public class ShareFileVO {
    private String title;
    private List<FileVO> files;
}
