package com.techstore.bank_system.application.notification.port;

/**
 * Порт отправки пользовательских уведомлений (email/смс и т.п.).
 */
public interface UserNotificationPort {
    boolean sendEmail(String to, String subject, String body);
}

