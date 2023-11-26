package com.cen.websky.controller;

import com.cen.websky.pojo.po.Captcha;
import com.cen.websky.pojo.po.User;
import com.cen.websky.pojo.vo.Result;
import com.cen.websky.service.RegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
@CrossOrigin(origins = "*")
public class RegisterController {
    private final RegisterService registerService;

    @PostMapping()
    public Result register(@RequestBody User user) {
        return registerService.register(user);
    }

    @PatchMapping("/status")
    public Result status(@RequestBody Captcha captcha) {
        return registerService.status(captcha);
    }
}
