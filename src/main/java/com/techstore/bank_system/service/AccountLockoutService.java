package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.entity.UserSecurityCode;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.repository.UserSecurityCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
public class AccountLockoutService {

    private final UserRepository userRepository;
    private final UserSecurityCodeRepository codeRepository;
    private final EmailService emailService;
    private final SecurityCodeService securityCodeService;

    @Value("${app.security.lockout.max-attempts:3}")
    private int maxAttempts;

    /**
     * Если 0 — блокировка "до подтверждения" (ставим lockoutUntil далеко в будущее).
     * Если >0 — блокировка на N минут.
     */
    @Value("${app.security.lockout.minutes:0}")
    private int lockoutMinutes;

    @Value("${app.security.unlock-code.ttl-minutes:10}")
    private int unlockCodeTtlMinutes;

    public AccountLockoutService(UserRepository userRepository,
                                 UserSecurityCodeRepository codeRepository,
                                 EmailService emailService,
                                 SecurityCodeService securityCodeService) {
        this.userRepository = userRepository;
        this.codeRepository = codeRepository;
        this.emailService = emailService;
        this.securityCodeService = securityCodeService;
    }

    public boolean isLocked(User user) {
        if (user == null) return false;
        LocalDateTime until = user.getLockoutUntil();
        return until != null && until.isAfter(LocalDateTime.now());
    }

    public void assertNotLocked(User user) {
        if (isLocked(user)) {
            throw new AccountLockedException("Аккаунт заблокирован. Проверьте почту и разблокируйте вход кодом.");
        }
    }

    @Transactional
    public void onLoginSuccess(User user) {
        if (user == null) return;
        boolean changed = false;

        if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() != 0) {
            user.setFailedLoginAttempts(0);
            changed = true;
        }

        // если блокировка истекла — уберём
        if (user.getLockoutUntil() != null && !user.getLockoutUntil().isAfter(LocalDateTime.now())) {
            user.setLockoutUntil(null);
            changed = true;
        }

        if (changed) {
            userRepository.save(user);
        }
    }

    @Transactional
    public void onLoginFailure(User user, String ip, String userAgent) {
        if (user == null) return;

        int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts());
        attempts++;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            LocalDateTime until;
            if (lockoutMinutes > 0) {
                until = LocalDateTime.now().plusMinutes(lockoutMinutes);
            } else {
                // фактически "до разблокировки" — ставим далеко
                until = LocalDateTime.now().plusDays(365);
            }
            user.setLockoutUntil(until);

            UnlockCode unlock = createUnlockCode(user);
            EmailMessage msg = buildAccountLockedEmail(user, unlock.code(), until, ip, userAgent, unlock.reference());

            try {
                boolean ok = emailService.sendEmail(user.getEmail(), msg.subject(), msg.text());
                if (!ok) {
                    System.err.println("❌ Lockout email was not sent (EmailService returned false). user=" + user.getEmail());
                }
            } catch (Exception e) {
                System.err.println("❌ Failed to send lockout email to " + user.getEmail() + ": " + e.getMessage());
            }
        }

        userRepository.save(user);
    }

    /**
     * Отдельный сценарий: пользователь жмёт "получить код".
     * Не увеличивает failedLoginAttempts.
     */
    @Transactional
    public void sendUnlockCodeEmail(User user, String ip, String userAgent) {
        if (user == null) return;
        if (user.getEmail() == null || user.getEmail().isBlank()) return;

        UnlockCode unlock = createUnlockCode(user);

        LocalDateTime until = user.getLockoutUntil();
        if (until == null) {
            until = LocalDateTime.now().plusMinutes(unlockCodeTtlMinutes);
        }

        EmailMessage msg = buildUnlockEmail(user, unlock.code(), until, ip, userAgent, unlock.reference());
        try {
            boolean ok = emailService.sendEmail(user.getEmail(), msg.subject(), msg.text());
            if (!ok) {
                System.err.println("❌ Unlock email was not sent (EmailService returned false). user=" + user.getEmail());
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to send unlock email to " + user.getEmail() + ": " + e.getMessage());
        }
    }

    @Transactional
    public UnlockCode createUnlockCode(User user) {
        codeRepository.deleteExpired(LocalDateTime.now());
        codeRepository.invalidateActiveCodes(user.getId(), UserSecurityCode.Purpose.UNLOCK);

        String code = securityCodeService.generate6Digits();
        String reference = securityCodeService.generateReference();

        UserSecurityCode entity = UserSecurityCode.builder()
                .user(user)
                .purpose(UserSecurityCode.Purpose.UNLOCK)
                .codeHash(sha256(code))
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(unlockCodeTtlMinutes))
                .consumedAt(null)
                .attempts(0)
                .reference(reference)
                .build();

        codeRepository.save(entity);
        return new UnlockCode(code, reference);
    }

    @Transactional
    public void unlockByCode(String email, String plainCode) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email обязателен");
        }
        if (plainCode == null || plainCode.isBlank()) {
            throw new IllegalArgumentException("Код обязателен");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        UserSecurityCode code = codeRepository.findActiveByUserAndPurpose(user.getId(), UserSecurityCode.Purpose.UNLOCK, LocalDateTime.now())
                .orElseThrow(() -> new IllegalArgumentException("Код не найден или истёк. Запросите новый."));

        int attempts = code.getAttempts() == null ? 0 : code.getAttempts();
        if (attempts >= 5) {
            code.setConsumedAt(LocalDateTime.now());
            codeRepository.save(code);
            throw new IllegalArgumentException("Слишком много попыток. Запросите новый код.");
        }

        String hash = sha256(plainCode);
        if (!hash.equalsIgnoreCase(code.getCodeHash())) {
            code.setAttempts(attempts + 1);
            codeRepository.save(code);
            throw new IllegalArgumentException("Неверный код");
        }

        code.setConsumedAt(LocalDateTime.now());
        codeRepository.save(code);

        user.setFailedLoginAttempts(0);
        user.setLockoutUntil(null);
        userRepository.save(user);
    }

    private EmailMessage buildAccountLockedEmail(User user, String code, LocalDateTime until, String ip, String userAgent, String reference) {
        String subject = "🚫 BankSystem: аккаунт заблокирован (код " + code + ")";
        String text = "Здравствуйте, " + safe(user.getFirstName()) + "!\n\n" +
                "Мы зафиксировали несколько неверных попыток входа в ваш аккаунт BankSystem.\n" +
                "Вход временно заблокирован.\n\n" +
                "🔐 Код разблокировки: " + code + "\n" +
                "🧾 Номер события: " + reference + "\n" +
                "⏳ Блокировка до: " + until + "\n" +
                "🌐 IP: " + safe(ip) + "\n" +
                "🖥️ Устройство: " + safe(userAgent) + "\n\n" +
                "Чтобы восстановить доступ, откройте /unlock.html и введите email + код.\n" +
                "Если это были не вы — после разблокировки сразу смените пароль.\n\n" +
                "Служба безопасности BankSystem";

        return new EmailMessage(subject, text);
    }

    private EmailMessage buildUnlockEmail(User user, String code, LocalDateTime until, String ip, String userAgent, String reference) {
        String subject = "🔓 BankSystem: код для разблокировки (" + code + ")";
        String text = "Здравствуйте, " + safe(user.getFirstName()) + "!\n\n" +
                "Вы запросили код для разблокировки входа в BankSystem.\n\n" +
                "🔐 Код: " + code + "\n" +
                "🧾 Номер заявки: " + reference + "\n" +
                "⏳ Код действует до: " + until + "\n\n" +
                "Детали запроса:\n" +
                "• IP: " + safe(ip) + "\n" +
                "• Устройство: " + safe(userAgent) + "\n\n" +
                "Служба безопасности BankSystem";

        return new EmailMessage(subject, text);
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "—" : v;
    }

    private String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash code", e);
        }
    }

    public record UnlockCode(String code, String reference) {}

    public record EmailMessage(String subject, String text) {}

    public static class AccountLockedException extends RuntimeException {
        public AccountLockedException(String message) {
            super(message);
        }
    }
}
