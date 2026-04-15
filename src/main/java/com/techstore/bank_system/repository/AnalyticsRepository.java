package com.techstore.bank_system.repository;

import com.techstore.bank_system.dto.analytics.BreakdownItemDto;
import com.techstore.bank_system.dto.analytics.PurchaseSummaryDto;
import com.techstore.bank_system.dto.analytics.TimeSeriesPointDto;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.TransactionType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager em;

    /**
     * MVP аналитики покупок:
     * считаем покупкой операции WITHDRAWAL (снятие/оплата), которые инициировал пользователь.
     */
    @Transactional(readOnly = true)
    public PurchaseSummaryDto purchaseSummary(Long userId,
                                             String accountNumber,
                                             CurrencyType currency,
                                             LocalDateTime from,
                                             LocalDateTime to) {

        String sql = """
                SELECT
                    COALESCE(SUM(t.amount), 0) AS total_amount,
                    COALESCE(COUNT(1), 0)      AS total_count,
                    COALESCE(AVG(t.amount), 0) AS avg_amount
                FROM transactions t
                LEFT JOIN accounts a ON a.id = t.from_account_id
                WHERE t.initiated_by_user_id = :userId
                  AND t.type = :type
                  AND (:currency IS NULL OR t.currency = :currency)
                  AND (:fromTs IS NULL OR t.created_at >= :fromTs)
                  AND (:toTs IS NULL OR t.created_at <= :toTs)
                  AND (:accNum IS NULL OR a.account_number = :accNum)
                """;

        Object[] row = (Object[]) em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("type", TransactionType.WITHDRAWAL.name())
                .setParameter("currency", currency == null ? null : currency.name())
                .setParameter("fromTs", from == null ? null : Timestamp.valueOf(from))
                .setParameter("toTs", to == null ? null : Timestamp.valueOf(to))
                .setParameter("accNum", (accountNumber == null || accountNumber.isBlank()) ? null : accountNumber)
                .getSingleResult();

        BigDecimal totalAmount = (BigDecimal) row[0];
        long totalCount = ((Number) row[1]).longValue();
        BigDecimal avgAmount = (BigDecimal) row[2];

        return new PurchaseSummaryDto(totalAmount, totalCount, avgAmount);
    }

    @Transactional(readOnly = true)
    public List<TimeSeriesPointDto> purchasesByDay(Long userId,
                                                   String accountNumber,
                                                   CurrencyType currency,
                                                   LocalDateTime from,
                                                   LocalDateTime to) {

        String sql = """
                SELECT
                    CONVERT(date, t.created_at) AS day,
                    COALESCE(SUM(t.amount), 0)  AS total_amount,
                    COUNT(1)                    AS total_count
                FROM transactions t
                LEFT JOIN accounts a ON a.id = t.from_account_id
                WHERE t.initiated_by_user_id = :userId
                  AND t.type = :type
                  AND (:currency IS NULL OR t.currency = :currency)
                  AND (:fromTs IS NULL OR t.created_at >= :fromTs)
                  AND (:toTs IS NULL OR t.created_at <= :toTs)
                  AND (:accNum IS NULL OR a.account_number = :accNum)
                GROUP BY CONVERT(date, t.created_at)
                ORDER BY day
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("type", TransactionType.WITHDRAWAL.name())
                .setParameter("currency", currency == null ? null : currency.name())
                .setParameter("fromTs", from == null ? null : Timestamp.valueOf(from))
                .setParameter("toTs", to == null ? null : Timestamp.valueOf(to))
                .setParameter("accNum", (accountNumber == null || accountNumber.isBlank()) ? null : accountNumber)
                .getResultList();

        return rows.stream().map(r -> {
            String day = String.valueOf(r[0]);
            BigDecimal total = (BigDecimal) r[1];
            long cnt = ((Number) r[2]).longValue();
            return new TimeSeriesPointDto(day, total, cnt);
        }).toList();
    }

    /**
     * Категории берём из description по префиксу: "CAT: <категория>; ...".
     * Если префикса нет — "Другое".
     */
    @Transactional(readOnly = true)
    public List<BreakdownItemDto> purchasesByCategory(Long userId,
                                                      String accountNumber,
                                                      CurrencyType currency,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      int limit) {

        String sql = """
                WITH src AS (
                    SELECT
                        CASE
                            WHEN t.description LIKE 'CAT:%' THEN
                                LTRIM(RTRIM(SUBSTRING(t.description, 5, CHARINDEX(';', t.description + ';') - 5)))
                            ELSE 'Другое'
                        END AS category,
                        t.amount AS amount
                    FROM transactions t
                    LEFT JOIN accounts a ON a.id = t.from_account_id
                    WHERE t.initiated_by_user_id = :userId
                      AND t.type = :type
                      AND (:currency IS NULL OR t.currency = :currency)
                      AND (:fromTs IS NULL OR t.created_at >= :fromTs)
                      AND (:toTs IS NULL OR t.created_at <= :toTs)
                      AND (:accNum IS NULL OR a.account_number = :accNum)
                )
                SELECT TOP (:limit)
                    category,
                    COALESCE(SUM(amount), 0) AS total_amount,
                    COUNT(1)                 AS total_count
                FROM src
                GROUP BY category
                ORDER BY total_amount DESC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("type", TransactionType.WITHDRAWAL.name())
                .setParameter("currency", currency == null ? null : currency.name())
                .setParameter("fromTs", from == null ? null : Timestamp.valueOf(from))
                .setParameter("toTs", to == null ? null : Timestamp.valueOf(to))
                .setParameter("accNum", (accountNumber == null || accountNumber.isBlank()) ? null : accountNumber)
                .setParameter("limit", Math.max(1, Math.min(limit, 50)))
                .getResultList();

        return rows.stream().map(r -> new BreakdownItemDto(
                String.valueOf(r[0]),
                (BigDecimal) r[1],
                ((Number) r[2]).longValue()
        )).toList();
    }

    /**
     * Мерчанта берём из description по префиксу: "MER: <merchant>; ...".
     */
    @Transactional(readOnly = true)
    public List<BreakdownItemDto> purchasesByMerchant(Long userId,
                                                      String accountNumber,
                                                      CurrencyType currency,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      int limit) {

        String sql = """
                WITH src AS (
                    SELECT
                        CASE
                            WHEN t.description LIKE 'MER:%' THEN
                                LTRIM(RTRIM(SUBSTRING(t.description, 5, CHARINDEX(';', t.description + ';') - 5)))
                            ELSE 'Не указан'
                        END AS merchant,
                        t.amount AS amount
                    FROM transactions t
                    LEFT JOIN accounts a ON a.id = t.from_account_id
                    WHERE t.initiated_by_user_id = :userId
                      AND t.type = :type
                      AND (:currency IS NULL OR t.currency = :currency)
                      AND (:fromTs IS NULL OR t.created_at >= :fromTs)
                      AND (:toTs IS NULL OR t.created_at <= :toTs)
                      AND (:accNum IS NULL OR a.account_number = :accNum)
                )
                SELECT TOP (:limit)
                    merchant,
                    COALESCE(SUM(amount), 0) AS total_amount,
                    COUNT(1)                 AS total_count
                FROM src
                GROUP BY merchant
                ORDER BY total_amount DESC
                """;

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .setParameter("type", TransactionType.WITHDRAWAL.name())
                .setParameter("currency", currency == null ? null : currency.name())
                .setParameter("fromTs", from == null ? null : Timestamp.valueOf(from))
                .setParameter("toTs", to == null ? null : Timestamp.valueOf(to))
                .setParameter("accNum", (accountNumber == null || accountNumber.isBlank()) ? null : accountNumber)
                .setParameter("limit", Math.max(1, Math.min(limit, 50)))
                .getResultList();

        return rows.stream().map(r -> new BreakdownItemDto(
                String.valueOf(r[0]),
                (BigDecimal) r[1],
                ((Number) r[2]).longValue()
        )).toList();
    }
}

