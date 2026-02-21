package com.techstore.bank_system.repository;

import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.User;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepository extends GenericRepository<Account, Long> {

    public AccountRepository() {
        super(Account.class);
    }

    @Override
    protected Long getEntityId(Account entity) {
        return entity.getId();
    }

    public Optional<Account> findByAccountNumber(String accountNumber) {
        try {
            Account account = entityManager.createQuery("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber", Account.class)
                    .setParameter("accountNumber", accountNumber)
                    .getSingleResult();
            return Optional.of(account);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Account> findByUser(User user) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.user = :user", Account.class)
                .setParameter("user", user)
                .getResultList();
    }

    public List<Account> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.user.id = :userId", Account.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Account> findActiveAccountsByUserId(Long userId) {
        return entityManager.createQuery("SELECT a FROM Account a WHERE a.user.id = :userId AND a.isActive = true AND a.isFrozen = false", Account.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}

