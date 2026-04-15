package com.techstore.bank_system.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Column(nullable = false)
    @ToString.Include
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Column(nullable = false)
    @ToString.Include
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть в правильном формате")
    @Column(unique = true, nullable = false)
    @ToString.Include
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Column(nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private String password;

    @Column(unique = true, nullable = true)
    private String passportNumber;

    @Column(unique = true, nullable = true)
    private String phoneNumber;

    @Column(nullable = true)
    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String patronymic;

    @Column(nullable = true)
    private String address;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime lastLoginAt;

    @Column(name = "failed_login_attempts", nullable = true, columnDefinition = "int default 0")
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    /**
     * Если значение в будущем — логин заблокирован.
     * Если null или в прошлом — можно входить.
     */
    @Column
    private LocalDateTime lockoutUntil;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private Set<Account> accounts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private Set<Loan> loans = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private Set<Deposit> deposits = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    @ToString.Exclude
    private Set<Card> cards = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<Role> roles = new HashSet<>();
}
