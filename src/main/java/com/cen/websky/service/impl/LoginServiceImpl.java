package com.cen.websky.service.impl;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.LoginService;
import com.cen.websky.service.UserService;
import com.cen.websky.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final UserService userService;

    @Override
    public Result login(User user) {
        //调用业务层：登录功能
        User loginUser = userService.login(user);

        //判断：登录用户是否存在
        if (loginUser != null) {
            //自定义信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", loginUser.getId());
            claims.put("username", loginUser.getUsername());
            claims.put("email", loginUser.getEmail());

            //使用JWT工具类，生成身份令牌
            String token = JwtUtils.generateJwt(claims);
            return Result.success(token);
        }
        return Result.error(401,"邮箱或密码错误");
    }
}
