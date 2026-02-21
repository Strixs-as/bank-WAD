package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.Card;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Repository
public class CardRepository extends GenericRepository<Card, Long> {
    public CardRepository() {
        super(Card.class);
    }
    @Override
    protected Long getEntityId(Card entity) {
        return entity.getId();
    }
    public Optional<Card> findByCardNumber(String cardNumber) {
        try {
            return Optional.of(entityManager.createQuery("SELECT c FROM Card c WHERE c.cardNumber = :cardNumber", Card.class)
                    .setParameter("cardNumber", cardNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public List<Card> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT c FROM Card c WHERE c.user.id = :userId ORDER BY c.createdAt DESC", Card.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    public List<Card> findByAccountId(Long accountId) {
        return entityManager.createQuery("SELECT c FROM Card c WHERE c.account.id = :accountId ORDER BY c.createdAt DESC", Card.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }
    public List<Card> findActiveCards(Long userId) {
        return entityManager.createQuery("SELECT c FROM Card c WHERE c.user.id = :userId AND c.isActive = true AND c.isBlocked = false", Card.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
