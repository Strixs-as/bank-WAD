package com.techstore.bank_system.repository;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.ExchangeRate;
import org.springframework.stereotype.Repository;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

@Repository
public class ExchangeRateRepository extends GenericRepository<ExchangeRate, Long> {
    public ExchangeRateRepository() {
        super(ExchangeRate.class);
    }
    @Override
    protected Long getEntityId(ExchangeRate entity) {
        return entity.getId();
    }
    public Optional<ExchangeRate> findRate(CurrencyType from, CurrencyType to) {
        try {
            return Optional.of(entityManager.createQuery(
                    "SELECT r FROM ExchangeRate r WHERE r.fromCurrency = :from AND r.toCurrency = :to",
                    ExchangeRate.class)
                    .setParameter("from", from)
                    .setParameter("to", to)
                    .getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    public List<ExchangeRate> findByCurrency(CurrencyType currency) {
        return entityManager.createQuery(
                "SELECT r FROM ExchangeRate r WHERE r.fromCurrency = :currency OR r.toCurrency = :currency",
                ExchangeRate.class)
                .setParameter("currency", currency)
                .getResultList();
    }
}
