package com.cen.websky.controller;

import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        return userService.login(user);
    }

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return userService.register(user);
    }

    @PatchMapping("/register/verify")
    public Result registerVerification(@RequestBody Captcha captcha) {
        return userService.registerVerification(captcha);
    }

    @GetMapping("/userInfo")
    public Result queryUser(HttpServletRequest request) {
        User userInfo;
        try {
            userInfo = userService.getById(((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("用户查询失败");
        }
        return Result.success(userInfo);
    }

    @PatchMapping("/updatePassword")
    public Result updatePassword(String password, HttpServletRequest request) {
        try {
            userService.updatePassword(password, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("密码修改失败");
        }
        return Result.success("密码修改成功");
    }

    @PatchMapping("/updateUserName")
    public Result updateUserName(String userName, HttpServletRequest request) {
        try {
            userService.updateUserName(userName, ((Claims) request.getAttribute("userInfo")).get("id", Long.class));
        } catch (Exception e) {
            return Result.error("用户名修改失败");
        }
        return Result.success("用户名修改成功");
    }
}
