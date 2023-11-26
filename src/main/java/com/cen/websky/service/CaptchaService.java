package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.po.Captcha;

public interface CaptchaService extends IService<Captcha> {
    /**
     * 判断验证码是否正确
     *
     * @param captcha
     * @return
     */
    Boolean status(Captcha captcha);
}
