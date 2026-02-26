package com.techstore.bank_system.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String transactionId;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    @NotNull
    @DecimalMin(value = "0.01")
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;
    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;
    @Column(length = 500)
    private String description;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime completedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_account_id")
    @ToString.Exclude
    @JsonIgnore
    private Account fromAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_account_id")
    @ToString.Exclude
    @JsonIgnore
    private Account toAccount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_user_id")
    @ToString.Exclude
    @JsonIgnore
    private User initiatedBy;

    // Вычисляемые поля для JSON-ответа
    public String getFromAccountNumber() {
        return fromAccount != null ? fromAccount.getAccountNumber() : null;
    }
    public String getToAccountNumber() {
        return toAccount != null ? toAccount.getAccountNumber() : null;
    }
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
