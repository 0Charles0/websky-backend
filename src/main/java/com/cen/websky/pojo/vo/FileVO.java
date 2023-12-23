package com.cen.websky.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.net.URL;
import java.util.Date;

@Data
@TableName("FileVO")
public class FileVO {
    private String fileName;
    private URL url;
    private Long size;
    private Date updateTime;
}
