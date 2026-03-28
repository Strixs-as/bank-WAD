package com.techstore.bank_system.util;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component("appDataInitializer")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Override
    @Transactional
    public void run(String... args) {
        initializeRoles();
        initializeAdminUser();
        initializeExchangeRates();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("USER").isEmpty()) {
            roleRepository.save(Role.builder().name("USER").description("Обычный пользователь банка").build());
        }
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(Role.builder().name("ADMIN").description("Администратор системы").build());
        }
        if (roleRepository.findByName("MANAGER").isEmpty()) {
            roleRepository.save(Role.builder().name("MANAGER").description("Менеджер банка").build());
        }
    }

    private void initializeAdminUser() {
        // Если admin уже существует — пропускаем
        if (userRepository.findByEmail("admin@bank.kz").isPresent()) {
            return;
        }

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Роль ADMIN не найдена"));

        User admin = User.builder()
                .firstName("Admin")
                .lastName("System")
                .patronymic("Administrator")
                .email("admin@bank.kz")
                .password(passwordEncoder.encode("admin123"))
                .passportNumber("ADMIN000001")
                .phoneNumber("+70000000000")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("г. Алматы, ул. Банковская, 1")
                .isActive(true)
                .isVerified(true)
                .createdAt(LocalDateTime.now())
                .build();

        admin.getRoles().add(adminRole);
        userRepository.save(admin);

        System.out.println("✅ Администратор создан: admin@bank.kz / admin123");
    }

    private void initializeExchangeRates() {
        LocalDateTime now = LocalDateTime.now();

        // USD to RUB
        if (exchangeRateRepository.findRate(CurrencyType.USD, CurrencyType.RUB).isEmpty()) {
            ExchangeRate usdToRub = ExchangeRate.builder()
                    .fromCurrency(CurrencyType.USD)
                    .toCurrency(CurrencyType.RUB)
                    .rate(new BigDecimal("75.50"))
                    .updatedAt(now)
                    .build();
            exchangeRateRepository.save(usdToRub);
        }

        // EUR to RUB
        if (exchangeRateRepository.findRate(CurrencyType.EUR, CurrencyType.RUB).isEmpty()) {
            ExchangeRate eurToRub = ExchangeRate.builder()
                    .fromCurrency(CurrencyType.EUR)
                    .toCurrency(CurrencyType.RUB)
                    .rate(new BigDecimal("82.30"))
                    .updatedAt(now)
                    .build();
            exchangeRateRepository.save(eurToRub);
        }

        // USD to EUR
        if (exchangeRateRepository.findRate(CurrencyType.USD, CurrencyType.EUR).isEmpty()) {
            ExchangeRate usdToEur = ExchangeRate.builder()
                    .fromCurrency(CurrencyType.USD)
                    .toCurrency(CurrencyType.EUR)
                    .rate(new BigDecimal("0.92"))
                    .updatedAt(now)
                    .build();
            exchangeRateRepository.save(usdToEur);
        }
    }
}
