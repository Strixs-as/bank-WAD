package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.Deposit;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Repository
public class DepositRepository extends GenericRepository<Deposit, Long> {
    public DepositRepository() {
        super(Deposit.class);
    }
    @Override
    protected Long getEntityId(Deposit entity) {
        return entity.getId();
    }
    public Optional<Deposit> findByDepositNumber(String depositNumber) {
        try {
            return Optional.of(entityManager.createQuery("SELECT d FROM Deposit d WHERE d.depositNumber = :depositNumber", Deposit.class)
                    .setParameter("depositNumber", depositNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public List<Deposit> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT d FROM Deposit d WHERE d.user.id = :userId ORDER BY d.createdAt DESC", Deposit.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    public List<Deposit> findActiveDeposits() {
        return entityManager.createQuery("SELECT d FROM Deposit d WHERE d.closedAt IS NULL ORDER BY d.endDate ASC", Deposit.class)
                .getResultList();
    }
}
