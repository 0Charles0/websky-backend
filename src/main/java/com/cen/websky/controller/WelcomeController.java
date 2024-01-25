package com.cen.websky.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping("/welcome")
    public String hello() {
        System.out.println("Welcome WebSky ~");
        return "Welcome WebSky ~";
    }
}
