package com.cen.websky.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cen.websky.pojo.po.Captcha;

public interface CaptchaService extends IService<Captcha> {
    /**
     * 检验验证码有效性
     *
     * @param captcha
     * @return
     */
    Boolean isValidCaptcha(Captcha captcha);
}
