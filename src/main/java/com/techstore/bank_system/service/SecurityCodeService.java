package com.techstore.bank_system.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * Генерирует короткие коды подтверждения (OTP) для уведомлений.
 *
 * Важно: в текущей версии код НЕ используется для блокировки входа,
 * это только информирование пользователя ("Если это не вы — смените пароль").
 */
@Service
public class SecurityCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate6Digits() {
        int code = 100_000 + RANDOM.nextInt(900_000);
        return String.valueOf(code);
    }

    /**
     * Небольшой "reference" для письма, чтобы пользователю проще было сослаться.
     */
    public String generateReference() {
        // 8 символов: 2 буквы + 6 цифр
        String letters = "ABCDEFGHJKLMNPQRSTUVWXYZ";
        char a = letters.charAt(RANDOM.nextInt(letters.length()));
        char b = letters.charAt(RANDOM.nextInt(letters.length()));
        int n = 100_000 + RANDOM.nextInt(900_000);
        return "BS-" + a + b + "-" + n;
    }
}

