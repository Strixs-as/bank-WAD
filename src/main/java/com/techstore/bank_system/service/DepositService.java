package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.DepositRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class DepositService {

    @Autowired
    private DepositRepository depositRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Deposit createDeposit(Long userId,
                                BigDecimal amount,
                                Integer durationMonths,
                                CurrencyType currency,
                                Long accountId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (userOpt.isEmpty() || accountOpt.isEmpty()) {
            throw new RuntimeException("Пользователь или счёт не найден");
        }

        Account account = accountOpt.get();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств на счёте для открытия депозита");
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(durationMonths);

        Deposit deposit = Deposit.builder()
                .depositNumber(generateDepositNumber())
                .amount(amount)
                .interestRate(getDepositRate(durationMonths))
                .durationMonths(durationMonths)
                .currency(currency)
                .startDate(startDate)
                .endDate(endDate)
                .accruedInterest(BigDecimal.ZERO)
                .user(userOpt.get())
                .account(account)
                .createdAt(LocalDateTime.now())
                .build();

        // Блокируем сумму депозита на счёте
        account.setBlockedAmount(account.getBlockedAmount().add(amount));
        account.updateAvailableBalance();
        accountRepository.save(account);

        return depositRepository.save(deposit);
    }

    @Transactional
    public Deposit closeDeposit(Long depositId) {
        Optional<Deposit> depositOpt = depositRepository.findById(depositId);
        if (depositOpt.isEmpty()) {
            throw new RuntimeException("Депозит не найден");
        }

        Deposit deposit = depositOpt.get();
        if (deposit.getClosedAt() != null) {
            throw new RuntimeException("Депозит уже закрыт");
        }

        // Начисляем проценты на момент закрытия
        BigDecimal interest = calculateAccruedInterest(deposit);
        deposit.setAccruedInterest(interest);

        // Возвращаем сумму депозита + проценты на счёт
        Account account = deposit.getAccount();
        account.setBlockedAmount(account.getBlockedAmount().subtract(deposit.getAmount()));
        account.setBalance(account.getBalance().add(deposit.getAmount()).add(interest));
        account.updateAvailableBalance();
        accountRepository.save(account);

        deposit.setClosedAt(LocalDateTime.now());
        return depositRepository.save(deposit);
    }

    public List<Deposit> getUserDeposits(Long userId) {
        return depositRepository.findByUserId(userId);
    }

    public BigDecimal calculateAccruedInterest(Deposit deposit) {
        if (deposit.getClosedAt() != null) {
            return deposit.getAccruedInterest();
        }

        long daysElapsed = ChronoUnit.DAYS.between(deposit.getStartDate(), LocalDate.now());
        double interestRate = deposit.getInterestRate().doubleValue() / 100.0 / 365.0;

        return deposit.getAmount()
                .multiply(BigDecimal.valueOf(interestRate * daysElapsed))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getDepositRate(Integer durationMonths) {
        if (durationMonths >= 24) return new BigDecimal("4.5");
        if (durationMonths >= 12) return new BigDecimal("3.5");
        if (durationMonths >= 6) return new BigDecimal("2.5");
        return new BigDecimal("1.5");
    }

    private String generateDepositNumber() {
        Random random = new Random();
        return "DEP" + System.currentTimeMillis() + random.nextInt(1000);
    }
}
