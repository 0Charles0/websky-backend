package com.cen.websky.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cen.websky.mapper.UserMapper;
import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.CaptchaService;
import com.cen.websky.service.EmailService;
import com.cen.websky.service.UserService;
import com.cen.websky.utils.JwtUtils;
import com.cen.websky.utils.ValidateCodeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final EmailService emailService;
    private final CaptchaService captchaService;

    @Override
    public Result checkEmptyEmailAndPassword(User user) {
        // 判断邮箱和密码是否为空
        boolean isEmptyEmail = StringUtils.isEmpty(user.getEmail());
        boolean isEmptyPassword = StringUtils.isEmpty(user.getPassword());
        if (isEmptyEmail && isEmptyPassword) {
            return Result.error("邮箱和密码都为空");
        } else if (isEmptyEmail) {
            return Result.error("邮箱为空");
        } else if (isEmptyPassword) {
            return Result.error("密码为空");
        }
        return null;
    }

    @Override
    public Result login(User user) {
        Result result = checkEmptyEmailAndPassword(user);
        if (result != null) {
            return result;
        }
        // 取得请求邮箱的用户信息
        User loginUser = getByEmail(user.getEmail());
        // 判断是否有该邮箱用户
        if (loginUser != null) {
            // 判断密码是否正确
            if (ObjectUtils.nullSafeEquals(loginUser.getPassword(), user.getPassword())) {
                // 检测账号是否启用
                if (loginUser.getStatus()) {
                    // 自定义 token 信息
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("id", loginUser.getId());
                    claims.put("username", loginUser.getUsername());
                    claims.put("email", loginUser.getEmail());
                    // 使用JWT工具类，生成身份令牌
                    String token = JwtUtils.generateJwt(claims);
                    return Result.success(token);
                }
                return Result.error(403, "该账号未完成邮箱验证 或 账号已冻结");
            }
        }
        return Result.error(401, "邮箱或密码错误");
    }

    @Override
    public Result register(User user) {
        Result result = checkEmptyEmailAndPassword(user);
        if (result != null) {
            return result;
        }
        // 判断邮箱是否被注册过
        if (getByEmail(user.getEmail()) == null) {
            // 初始化用户，账号未启用（status 字段默认为 0/false）
            save(user);
            // 生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4);
            // 发送验证邮件
            emailService.sendVerificationLink(user.getEmail(), code);
            // 将验证码和对应邮箱保存到captcha表中临时存储，等待验证
            Captcha captcha = new Captcha();
            captcha.setEmail(user.getEmail());
            captcha.setCode(code);
            captchaService.save(captcha);
            return Result.success("注册验证邮件已发送，请点击邮件内链接完成注册");
        }
        return Result.error(409, "注册失败，该邮箱已被注册");
    }

    @Override
    public Result registerVerification(Captcha captcha) {
        // 判断邮箱和验证码是否正确
        if (captchaService.isValidCaptcha(captcha)) {
            // 激活账号
            toggleStatusByEmail(captcha.getEmail());
            return Result.success("注册成功");
        }
        return Result.error("注册验证失败");
    }

    @Override
    public void toggleStatusByEmail(String email) {
        // 查询当前状态
        User user = getByEmail(email);
        // 反转状态
        user.setStatus(!user.getStatus());
        updateById(user);
    }

    @Override
    public User getByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email).one();
    }
}
