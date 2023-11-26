package com.cen.websky.service;

import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;

public interface RegisterService {
    /**
     * 用户注册
     * @param user
     * @return
     */
    Result register(User user);

    /**
     * 注册验证
     *
     * @param captcha
     * @return
     */
    Result status(Captcha captcha);
}
