package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailTemplateService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public EmailMessage buildLoginAlert(User user, String ip, String userAgent, String otpCode, String reference) {
        String displayName = safe(user.getFirstName());
        String subject = "⚠️ BankSystem: вход в аккаунт (код " + otpCode + ")";

        StringBuilder sb = new StringBuilder();
        sb.append("Здравствуйте, ").append(displayName).append("!\n\n")
          .append("Мы зафиксировали вход в ваш аккаунт BankSystem.\n\n")
          .append("🔐 Код безопасности: ").append(otpCode).append("\n")
          .append("🧾 Номер события: ").append(reference).append("\n\n")
          .append("📅 Время: ").append(LocalDateTime.now().format(TS)).append("\n")
          .append("🌐 IP-адрес: ").append(safe(ip)).append("\n")
          .append("🖥️ Устройство/браузер: ").append(shortUa(userAgent)).append("\n\n")
          .append("Если это были не вы:\n")
          .append("1) Срочно смените пароль\n")
          .append("2) Проверьте активные сессии\n")
          .append("3) Сообщите в поддержку (укажите номер события: ").append(reference).append(")\n\n")
          .append("С уважением,\n")
          .append("Служба безопасности BankSystem");

        return new EmailMessage(subject, sb.toString());
    }

    public EmailMessage buildWelcome(User user) {
        String displayName = safe(user.getFirstName());
        String subject = "🎉 Добро пожаловать в BankSystem, " + displayName + "!";

        StringBuilder sb = new StringBuilder();
        sb.append("Здравствуйте, ").append(displayName).append("!\n\n")
          .append("Ваш аккаунт в BankSystem успешно создан.\n\n")
          .append("✅ Логин: ").append(safe(user.getEmail())).append("\n")
          .append("📅 Дата регистрации: ").append(LocalDateTime.now().format(TS)).append("\n\n")
          .append("Полезно знать:\n")
          .append("• Никому не сообщайте пароль и коды безопасности\n")
          .append("• Настройте актуальные контакты в профиле\n")
          .append("• Если вы не регистрировались — проигнорируйте письмо\n\n")
          .append("С уважением,\n")
          .append("Команда BankSystem");

        return new EmailMessage(subject, sb.toString());
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "—" : v;
    }

    private String shortUa(String ua) {
        if (ua == null || ua.isBlank()) return "—";
        String trimmed = ua.trim();
        return trimmed.length() > 160 ? trimmed.substring(0, 160) + "…" : trimmed;
    }

    public record EmailMessage(String subject, String text) {}
}

