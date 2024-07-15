package com.cen.websky.service;

public interface EmailService {
    /**
     * 发送验证链接
     * @param to
     * @param code
     */
    void sendVerificationLink(String to, String code);
}
