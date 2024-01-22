package com.cen.websky.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("share_file")
public class ShareFile {
    private Long id;
    private String title;
    private String path;
    private Long userId;
    private boolean open;
}