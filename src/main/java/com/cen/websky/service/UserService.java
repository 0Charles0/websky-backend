package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;

public interface UserService extends IService<User> {
    /**
     * 检测空邮箱和密码
     *
     * @param user
     * @return
     */
    Result checkEmptyEmailAndPassword(User user);

    /**
     * 用户登录
     *
     * @param user
     * @return
     */
    Result login(User user);

    /**
     * 用户注册
     *
     * @param user
     * @return
     */
    Result register(User user);

    /**
     * 根据 email 反转账号状态
     *
     * @param email
     */
    void toggleStatusByEmail(String email);

    /**
     * 根据 email 查询用户信息
     *
     * @param email
     * @return
     */
    User getByEmail(String email);

    /**
     * 注册验证
     *
     * @param captcha
     * @return
     */
    Result registerVerification(Captcha captcha);
}
