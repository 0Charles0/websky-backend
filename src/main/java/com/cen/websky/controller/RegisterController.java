package com.cen.websky.controller;

import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        return registerService.register(user);
    }
}
