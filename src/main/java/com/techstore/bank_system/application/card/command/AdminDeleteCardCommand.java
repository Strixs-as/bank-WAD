package com.techstore.bank_system.application.card.command;

import jakarta.validation.constraints.NotNull;

/**
 * CQRS Command: admin удаляет (soft-delete) карту.
 */
public record AdminDeleteCardCommand(
        @NotNull Long cardId,
        String reason,
        String performedBy
) {
}

