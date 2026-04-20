package com.techstore.bank_system.infrastructure.card;

import com.techstore.bank_system.application.card.port.CardAdminPort;
import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.repository.CardRepository;

import java.util.Optional;

/**
 * Legacy infrastructure adapter: оставлен для обратной совместимости.
 *
 * ВАЖНО: сейчас НЕ регистрируется как Spring bean, чтобы не конфликтовать с основным адаптером.
 * Если понадобится, включим через отдельную конфигурацию/профиль.
 */
@Deprecated(forRemoval = false)
public class CardAdminAdapter implements CardAdminPort {
    private final CardRepository cardRepository;

    public CardAdminAdapter(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Optional<Card> findById(Long id) {
        // Админу можно находить даже soft-deleted карту
        return cardRepository.findById(id);
    }

    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }
}
