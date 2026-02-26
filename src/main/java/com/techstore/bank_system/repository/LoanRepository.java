package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.Loan;
import com.techstore.bank_system.entity.LoanStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Repository
public class LoanRepository extends GenericRepository<Loan, Long> {
    public LoanRepository() {
        super(Loan.class);
    }
    @Override
    protected Long getEntityId(Loan entity) {
        return entity.getId();
    }
    @Transactional(readOnly = true)
    public Optional<Loan> findByLoanNumber(String loanNumber) {
        try {
            return Optional.of(entityManager.createQuery("SELECT l FROM Loan l WHERE l.loanNumber = :loanNumber", Loan.class)
                    .setParameter("loanNumber", loanNumber)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    @Transactional(readOnly = true)
    public List<Loan> findByUserId(Long userId) {
        return entityManager.createQuery("SELECT l FROM Loan l WHERE l.user.id = :userId ORDER BY l.createdAt DESC", Loan.class)
                .setParameter("userId", userId)
                .getResultList();
    }
    @Transactional(readOnly = true)
    public List<Loan> findByStatus(LoanStatus status) {
        return entityManager.createQuery("SELECT l FROM Loan l WHERE l.status = :status ORDER BY l.createdAt DESC", Loan.class)
                .setParameter("status", status)
                .getResultList();
    }
    @Transactional(readOnly = true)
    public List<Loan> findActiveLoans() {
        return entityManager.createQuery("SELECT l FROM Loan l WHERE l.status = com.techstore.bank_system.entity.LoanStatus.ACTIVE ORDER BY l.endDate ASC", Loan.class)
                .getResultList();
    }
}
