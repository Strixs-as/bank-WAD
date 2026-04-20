package com.techstore.bank_system.infrastructure.card;

import com.techstore.bank_system.application.card.port.CardAdminPort;
import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.repository.CardRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Infrastructure adapter: связывает application port с существующим CardRepository.
 */
@Service
@Primary
public class JpaCardAdminAdapter implements CardAdminPort {
    private final CardRepository cardRepository;

    public JpaCardAdminAdapter(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public Optional<Card> findById(Long id) {
        return cardRepository.findById(id);
    }

    @Override
    public Card save(Card card) {
        return cardRepository.save(card);
    }
}
