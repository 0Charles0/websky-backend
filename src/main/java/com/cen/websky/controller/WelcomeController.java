package com.cen.websky.controller;

import com.cen.websky.pojo.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping("/welcome")
    public Result hello() {
        System.out.println("Welcome WebSky ~");
        return Result.success("Welcome WebSky ~");
    }
}
