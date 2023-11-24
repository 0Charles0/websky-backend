package com.cen.websky.controller;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/")
    public Result list(){
        List<User> users = userService.list();
        return Result.success(users);
    }
}
