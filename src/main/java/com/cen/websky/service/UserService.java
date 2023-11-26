package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.po.User;

public interface UserService extends IService<User> {
    /**
     * 用户登录
     *
     * @param user
     * @return
     */
    User login(User user);

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    User register(User user);

    void status(String email);
}
