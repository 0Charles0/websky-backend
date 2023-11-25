package com.cen.websky.utils;

import java.util.Random;

public class VerificationCodeGenerator {
    public static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10)); // 生成 0 到 9 的随机数字
        }
        return code.toString();
    }
}
