package com.techstore.bank_system.application.card.command;

import com.techstore.bank_system.application.card.port.CardAdminPort;
import com.techstore.bank_system.application.notification.port.UserNotificationPort;
import com.techstore.bank_system.entity.Card;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AdminDeleteCardHandler {
    private final CardAdminPort cardPort;
    private final UserNotificationPort notificationPort;

    public AdminDeleteCardHandler(
            CardAdminPort cardPort,
            UserNotificationPort notificationPort
    ) {
        this.cardPort = cardPort;
        this.notificationPort = notificationPort;
    }

    /**
     * Soft-delete: помечаем карту как удалённую (isDeleted=true) и дополнительно блокируем/деактивируем.
     */
    @Transactional
    public AdminDeleteCardResult handle(AdminDeleteCardCommand cmd) {
        Card card = cardPort.findById(cmd.cardId())
                .orElseThrow(() -> new IllegalArgumentException("Карта не найдена: id=" + cmd.cardId()));

        LocalDateTime now = LocalDateTime.now();

        // Блокировка/деактивация (безопасность)
        if (!Boolean.TRUE.equals(card.getIsBlocked())) {
            card.setIsBlocked(true);
            card.setBlockedAt(now);
        }
        if (!Boolean.FALSE.equals(card.getIsActive())) {
            card.setIsActive(false);
        }

        // Soft-delete (аудит)
        if (!Boolean.TRUE.equals(card.getIsDeleted())) {
            card.setIsDeleted(true);
            card.setDeletedAt(now);
        }

        String reason = (cmd.reason() == null || cmd.reason().isBlank()) ? "Удалено администратором" : cmd.reason();
        card.setDeletedReason(reason);

        String by = (cmd.performedBy() == null || cmd.performedBy().isBlank()) ? "admin" : cmd.performedBy();
        card.setDeletedBy(by);

        Card saved = cardPort.save(card);

        boolean emailAttempted = false;
        boolean emailSent = false;
        String userEmail = (saved.getUser() != null) ? saved.getUser().getEmail() : null;

        if (userEmail != null && !userEmail.isBlank()) {
            emailAttempted = true;
            String masked = mask(saved.getCardNumber());
            String when = now.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            String subject = "BankSystem: карта заблокирована и удалена";
            String body = "Здравствуйте!\n\n" +
                    "Администратор удалил вашу карту из BankSystem (карта заблокирована и деактивирована).\n\n" +
                    "Карта: " + masked + "\n" +
                    "Время: " + when + "\n" +
                    "Причина: " + reason + "\n" +
                    "Кто выполнил: " + by + "\n\n" +
                    "Если это были не вы — срочно смените пароль и обратитесь в поддержку.\n\n" +
                    "© BankSystem\n";

            emailSent = notificationPort.sendEmail(userEmail, subject, body);
        }

        return AdminDeleteCardResult.ok(saved.getId(), userEmail, emailAttempted, emailSent);
    }

    private static String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) return "**** **** **** ****";
        String last4 = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
