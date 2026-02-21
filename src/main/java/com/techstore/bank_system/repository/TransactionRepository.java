package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.Transaction;
import com.techstore.bank_system.entity.TransactionStatus;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionRepository extends GenericRepository<Transaction, Long> {
    public TransactionRepository() {
        super(Transaction.class);
    }
    @Override
    protected Long getEntityId(Transaction entity) {
        return entity.getId();
    }
    public Optional<Transaction> findByTransactionId(String transactionId) {
        try {
            return Optional.of(entityManager.createQuery("SELECT t FROM Transaction t WHERE t.transactionId = :transactionId", Transaction.class)
                    .setParameter("transactionId", transactionId)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public List<Transaction> findByAccountId(Long accountId) {
        return entityManager.createQuery(
                "SELECT t FROM Transaction t WHERE t.fromAccount.id = :accountId OR t.toAccount.id = :accountId ORDER BY t.createdAt DESC",
                Transaction.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }
    public List<Transaction> findByAccountIdAndDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return entityManager.createQuery(
                "SELECT t FROM Transaction t WHERE (t.fromAccount.id = :accountId OR t.toAccount.id = :accountId) " +
                "AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC",
                Transaction.class)
                .setParameter("accountId", accountId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
    public List<Transaction> findByStatus(TransactionStatus status) {
        return entityManager.createQuery("SELECT t FROM Transaction t WHERE t.status = :status ORDER BY t.createdAt DESC", Transaction.class)
                .setParameter("status", status)
                .getResultList();
    }
    public List<Transaction> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return entityManager.createQuery(
                "SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC",
                Transaction.class)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}
