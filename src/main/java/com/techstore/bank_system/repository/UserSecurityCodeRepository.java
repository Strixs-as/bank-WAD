package com.techstore.bank_system.repository;

import com.techstore.bank_system.entity.UserSecurityCode;
import jakarta.persistence.NoResultException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserSecurityCodeRepository extends GenericRepository<UserSecurityCode, Long> {

    public UserSecurityCodeRepository() {
        super(UserSecurityCode.class);
    }

    @Override
    protected Long getEntityId(UserSecurityCode entity) {
        return entity.getId();
    }

    @Transactional(readOnly = true)
    public Optional<UserSecurityCode> findActiveByUserAndPurpose(Long userId,
                                                                 UserSecurityCode.Purpose purpose,
                                                                 LocalDateTime now) {
        try {
            UserSecurityCode code = entityManager.createQuery(
                            "SELECT c FROM UserSecurityCode c " +
                                    "WHERE c.user.id = :uid AND c.purpose = :purpose " +
                                    "AND c.consumedAt IS NULL AND c.expiresAt > :now " +
                                    "ORDER BY c.createdAt DESC",
                            UserSecurityCode.class)
                    .setParameter("uid", userId)
                    .setParameter("purpose", purpose)
                    .setParameter("now", now)
                    .setMaxResults(1)
                    .getSingleResult();
            return Optional.of(code);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public int invalidateActiveCodes(Long userId, UserSecurityCode.Purpose purpose) {
        return entityManager.createQuery(
                        "UPDATE UserSecurityCode c SET c.consumedAt = :now " +
                                "WHERE c.user.id = :uid AND c.purpose = :purpose AND c.consumedAt IS NULL")
                .setParameter("now", LocalDateTime.now())
                .setParameter("uid", userId)
                .setParameter("purpose", purpose)
                .executeUpdate();
    }

    @Transactional
    public int deleteExpired(LocalDateTime now) {
        return entityManager.createQuery("DELETE FROM UserSecurityCode c WHERE c.expiresAt <= :now")
                .setParameter("now", now)
                .executeUpdate();
    }
}

