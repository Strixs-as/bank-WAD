package com.techstore.bank_system.application.card.command;

/**
 * Результат админ-операции удаления карты.
 */
public record AdminDeleteCardResult(
        boolean success,
        Long cardId,
        String userEmail,
        boolean emailAttempted,
        boolean emailSent,
        String message
) {
    public static AdminDeleteCardResult ok(Long cardId, String userEmail, boolean emailAttempted, boolean emailSent) {
        return new AdminDeleteCardResult(true, cardId, userEmail, emailAttempted, emailSent, "OK");
    }

    public static AdminDeleteCardResult fail(Long cardId, String message) {
        return new AdminDeleteCardResult(false, cardId, null, false, false, message);
    }
}

