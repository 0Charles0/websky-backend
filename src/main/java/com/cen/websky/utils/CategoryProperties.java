package com.cen.websky.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*分类格式相关配置*/
@Data
@Component
@ConfigurationProperties(prefix = "category")
public class CategoryProperties {
    // 图片
    private String picture;
    // 文档
    private String document;
    // 视频
    private String video;
    // 音频
    private String audio;
}