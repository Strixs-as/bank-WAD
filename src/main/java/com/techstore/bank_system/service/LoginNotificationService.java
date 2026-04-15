package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginNotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoginNotificationService.class);

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecurityCodeService securityCodeService;
    private final EmailTemplateService emailTemplateService;

    /**
     * Куда дополнительно уведомлять администратора о входах.
     * Можно выключить, оставив пустым.
     */
    @Value("${app.notifications.admin-email:}")
    private String adminNotifyEmail;

    public LoginNotificationService(UserRepository userRepository,
                                    EmailService emailService,
                                    SecurityCodeService securityCodeService,
                                    EmailTemplateService emailTemplateService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.securityCodeService = securityCodeService;
        this.emailTemplateService = emailTemplateService;
    }

    public void notifyLogin(String email, String ip, String userAgent) {
        if (email == null || email.isBlank()) {
            log.debug("notifyLogin: auth email is blank -> skip");
            return;
        }

        log.debug("Login detected for email={}", email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            log.debug("User not found in DB by email={} -> skip sending", email);
            return;
        }

        User user = userOpt.get();

        String otp = securityCodeService.generate6Digits();
        String reference = securityCodeService.generateReference();

        // 1) Пользователю
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            EmailTemplateService.EmailMessage msg = emailTemplateService.buildLoginAlert(user, ip, userAgent, otp, reference);
            try {
                boolean ok = emailService.sendEmail(user.getEmail(), msg.subject(), msg.text());
                log.debug("notifyLogin: email to user={} result={}", user.getEmail(), ok);
            } catch (Exception e) {
                log.warn("notifyLogin: failed to send email to user={} :: {}", user.getEmail(), e.getMessage());
            }
        }

        // 2) Админу (опционально)
        if (adminNotifyEmail != null && !adminNotifyEmail.isBlank()) {
            String adminSubject = "[ADMIN] Login event " + reference + " :: " + safe(user.getEmail());
            String adminText = "Событие входа зарегистрировано.\n\n"
                    + "Пользователь: " + safe(user.getEmail()) + "\n"
                    + "ФИО: " + safe(user.getFirstName()) + " " + safe(user.getLastName()) + "\n"
                    + "Код: " + otp + "\n"
                    + "IP: " + safe(ip) + "\n"
                    + "User-Agent: " + safe(userAgent);

            try {
                boolean ok = emailService.sendEmail(adminNotifyEmail, adminSubject, adminText);
                log.debug("notifyLogin: email to admin={} result={}", adminNotifyEmail, ok);
            } catch (Exception e) {
                log.warn("notifyLogin: failed to send email to admin={} :: {}", adminNotifyEmail, e.getMessage());
            }
        }
    }

    private String safe(String v) {
        return v == null ? "—" : v;
    }
}
