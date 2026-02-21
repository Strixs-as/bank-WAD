package com.techstore.bank_system.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false, length = 16)
    private String cardNumber;
    @NotBlank
    @Column(nullable = false, length = 50)
    private String cardHolder;
    @NotBlank
    @Column(nullable = false, length = 4)
    private String cvv;
    @NotNull
    @Column(nullable = false)
    private LocalDate expiryDate;
    @Column(nullable = false)
    private Boolean isActive = true;
    @Column(nullable = false)
    private Boolean isBlocked = false;
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column
    private LocalDateTime blockedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @ToString.Exclude
    private Account account;
}
