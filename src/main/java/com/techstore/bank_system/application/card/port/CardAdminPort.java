package com.techstore.bank_system.application.card.port;

import com.techstore.bank_system.entity.Card;

import java.util.Optional;

/**
 * Порт для админ-операций с картами.
 */
public interface CardAdminPort {
    Optional<Card> findById(Long id);

    Card save(Card card);
}

