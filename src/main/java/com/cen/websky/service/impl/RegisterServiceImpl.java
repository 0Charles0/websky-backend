package com.cen.websky.service.impl;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.EmailService;
import com.cen.websky.service.LoginService;
import com.cen.websky.service.RegisterService;
import com.cen.websky.service.UserService;
import com.cen.websky.utils.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final UserService userService;
    private final LoginService loginService;
    private final EmailService emailService;

    @Override
    public Result register(User user) {
        //调用业务层：注册功能
        User registerUser = userService.register(user);
        //判断：邮箱是否被注册过
        if (registerUser != null) {
            // 生成验证码
            String verificationCode = VerificationCodeGenerator.generateCode(6);
            // 发送邮件
            emailService.sendVerificationCode(user.getEmail(), verificationCode);
            return Result.success("注册验证邮件已发送，请点击邮件链接完成注册");
        } else {
            return Result.error(409, "注册失败：该邮箱已被注册");
        }
    }
}
