package com.cen.websky.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("captcha")
public class Captcha {
    private String email;
    private String code;
}
