package com.cen.websky.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("file")
public class File {
    private Long id;
    private String fileName;
    private String filePath;
    private Long userId;
}
