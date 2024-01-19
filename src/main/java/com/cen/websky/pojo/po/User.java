package com.cen.websky.pojo.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    private Long id;
    private String userName;
    private String password;
    private String email;
    private String image;
    private Boolean status;
    private LocalDateTime logoutTime;
}
