package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.CaptchaMapper;
import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.service.CaptchaService;
import org.springframework.stereotype.Service;

@Service
public class CaptchaServiceImpl extends ServiceImpl<CaptchaMapper, Captcha> implements CaptchaService {
    @Override
    public Boolean isValidCaptcha(Captcha captcha) {
        QueryWrapper<Captcha> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(Captcha::getEmail, captcha.getEmail())
                .eq(Captcha::getCode, captcha.getCode());
        if (getOne(wrapper, false) != null) {
            remove(wrapper);
            return true;
        }
        return false;
    }
}
