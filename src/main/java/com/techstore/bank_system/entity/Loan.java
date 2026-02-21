package com.techstore.bank_system.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "loans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String loanNumber;
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;
    @NotNull
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;
    @NotNull
    @Column(nullable = false)
    private Integer durationMonths;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime approvedAt;
    @Column
    private LocalDateTime closedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    private Account account;
    public BigDecimal calculateMonthlyPayment() {
        if (durationMonths == 0) return BigDecimal.ZERO;
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 4, java.math.RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
        BigDecimal monthlyPayment = principalAmount
                .multiply(monthlyRate.multiply(monthlyRate.add(BigDecimal.ONE).pow(durationMonths)))
                .divide(monthlyRate.add(BigDecimal.ONE).pow(durationMonths).subtract(BigDecimal.ONE), 2, java.math.RoundingMode.HALF_UP);
        return monthlyPayment;
    }
}
