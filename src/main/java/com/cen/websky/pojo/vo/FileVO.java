package com.cen.websky.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.net.URL;
import java.time.LocalDateTime;

@Data
@TableName("FileVO")
public class FileVO {
    private String fileName;
    private URL url;
    private Long size;
    private LocalDateTime updateTime;
    private String category;
}
