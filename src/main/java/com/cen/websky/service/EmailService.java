package com.cen.websky.service;

public interface EmailService {
    void sendVerificationCode(String to, String code);

}
