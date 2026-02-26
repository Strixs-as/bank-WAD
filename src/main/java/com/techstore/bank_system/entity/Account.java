package com.techstore.bank_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Номер счета обязателен")
    @Column(unique = true, nullable = false, length = 20)
    private String accountNumber;

    @NotNull(message = "Тип счета обязателен")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @NotNull(message = "Валюта обязательна")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    @Column(nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal blockedAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isFrozen = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "fromAccount", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<Transaction> sentTransactions = new HashSet<>();

    @OneToMany(mappedBy = "toAccount", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<Transaction> receivedTransactions = new HashSet<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private Set<Card> cards = new HashSet<>();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAvailableBalance() {
        this.availableBalance = this.balance.subtract(this.blockedAmount);
    }
}

