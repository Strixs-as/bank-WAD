package com.techstore.bank_system.infrastructure.notification;

import com.techstore.bank_system.application.notification.port.UserNotificationPort;
import com.techstore.bank_system.service.EmailService;
import org.springframework.stereotype.Component;

/**
 * Infrastructure adapter: отправка email через существующий EmailService.
 */
@Component
public class EmailUserNotificationAdapter implements UserNotificationPort {
    private final EmailService emailService;

    public EmailUserNotificationAdapter(EmailService emailService) {
        this.emailService = emailService;
    }

    @Override
    public boolean sendEmail(String to, String subject, String body) {
        try {
            emailService.sendEmail(to, subject, body);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

