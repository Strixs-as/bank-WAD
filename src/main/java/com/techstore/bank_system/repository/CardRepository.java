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
            return Optional.of(entityManager.createQuery(
                            "SELECT c FROM Card c WHERE c.cardNumber = :cardNumber AND (c.isDeleted = false OR c.isDeleted IS NULL)",
                            Card.class)
                    .setParameter("cardNumber", cardNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public List<Card> findByUserId(Long userId) {
        return entityManager.createQuery(
                        "SELECT c FROM Card c " +
                        "WHERE c.user.id = :userId " +
                        "AND (c.isDeleted = false OR c.isDeleted IS NULL) " +
                        "ORDER BY c.createdAt DESC",
                        Card.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    public List<Card> findByAccountId(Long accountId) {
        return entityManager.createQuery(
                        "SELECT c FROM Card c " +
                        "WHERE c.account.id = :accountId " +
                        "AND (c.isDeleted = false OR c.isDeleted IS NULL) " +
                        "ORDER BY c.createdAt DESC",
                        Card.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }
    public List<Card> findActiveCards(Long userId) {
        return entityManager.createQuery(
                        "SELECT c FROM Card c " +
                        "WHERE c.user.id = :userId " +
                        "AND (c.isDeleted = false OR c.isDeleted IS NULL) " +
                        "AND (c.isActive = true OR c.isActive IS NULL) " +
                        "AND (c.isBlocked = false OR c.isBlocked IS NULL) " +
                        "ORDER BY c.createdAt DESC",
                        Card.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * Для админки: по умолчанию без удалённых (как и раньше), но с учётом isDeleted.
     */
    public List<Card> findAdminVisibleCards() {
        return entityManager.createQuery(
                        "SELECT c FROM Card c " +
                        "WHERE (c.isDeleted = false OR c.isDeleted IS NULL) " +
                        "AND (c.isActive = true OR c.isActive IS NULL) " +
                        "AND (c.isBlocked = false OR c.isBlocked IS NULL) " +
                        "ORDER BY c.createdAt DESC",
                        Card.class)
                .getResultList();
    }

    /**
     * Для админки: все карты, включая удалённые.
     */
    public List<Card> findAdminAllCardsIncludingDeleted() {
        return entityManager.createQuery(
                        "SELECT c FROM Card c ORDER BY c.createdAt DESC",
                        Card.class)
                .getResultList();
    }
}
