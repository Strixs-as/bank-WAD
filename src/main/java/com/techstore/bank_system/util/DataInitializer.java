package com.techstore.bank_system.util;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component("dataInitializerUtil")
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ExchangeRateRepository exchangeRateRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeExchangeRates();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("USER").isEmpty()) {
            Role userRole = Role.builder()
                    .name("USER")
                    .description("Обычный пользователь банка")
                    .build();
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = Role.builder()
                    .name("ADMIN")
                    .description("Администратор системы")
                    .build();
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName("MANAGER").isEmpty()) {
            Role managerRole = Role.builder()
                    .name("MANAGER")
                    .description("Менеджер банка")
                    .build();
            roleRepository.save(managerRole);
        }
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
