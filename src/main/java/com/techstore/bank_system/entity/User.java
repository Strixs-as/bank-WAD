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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Имя обязательно")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Email должен быть в правильном формате")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @NotBlank(message = "Номер паспорта обязателен")
    @Column(unique = true, nullable = false)
    private String passportNumber;

    @NotBlank(message = "Номер телефона обязателен")
    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @NotNull(message = "Дата рождения обязательна")
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String patronymic;

    @NotBlank(message = "Адрес обязателен")
    @Column(nullable = false)
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Account> accounts = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Loan> loans = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Deposit> deposits = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @JsonIgnore
    private Set<Card> cards = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
}
