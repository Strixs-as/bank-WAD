package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.AccountType;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Account createAccount(Long userId, AccountType accountType, CurrencyType currency, BigDecimal initialDeposit) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Пользователь не найден");
        }

        User user = userOpt.get();

        // Генерируем уникальный номер счёта
        String accountNumber = generateAccountNumber();

        // Создаём новый счёт
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(accountType)
                .currency(currency)
                .balance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO)
                .availableBalance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO)
                .blockedAmount(BigDecimal.ZERO)
                .interestRate(getDefaultInterestRate(accountType))
                .isActive(true)
                .isFrozen(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    public List<Account> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }

    @Transactional
    public Account updateBalance(Long accountId, BigDecimal amount) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счёт не найден");
        }

        Account account = accountOpt.get();
        account.setBalance(account.getBalance().add(amount));
        account.updateAvailableBalance();
        return accountRepository.save(account);
    }

    @Transactional
    public void freezeAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счёт не найден");
        }

        Account account = accountOpt.get();
        account.setIsFrozen(true);
        accountRepository.save(account);
    }

    @Transactional
    public void unfreezeAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счёт не найден");
        }

        Account account = accountOpt.get();
        account.setIsFrozen(false);
        accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счёт не найден");
        }

        Account account = accountOpt.get();
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Нельзя закрыть счёт: баланс должен быть равен 0");
        }

        account.setIsActive(false);
        account.setClosedAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        // Генерируем 20-значный номер счёта
        for (int i = 0; i < 20; i++) {
            sb.append(random.nextInt(10));
        }

        String accountNumber = sb.toString();

        // Проверяем уникальность
        if (accountRepository.findByAccountNumber(accountNumber).isPresent()) {
            return generateAccountNumber();
        }

        return accountNumber;
    }

    private BigDecimal getDefaultInterestRate(AccountType accountType) {
        return switch (accountType) {
            case SAVINGS -> new BigDecimal("2.5");
            case CHECKING -> new BigDecimal("0.1");
            case DEPOSIT -> new BigDecimal("5.0");
            case LOAN -> BigDecimal.ZERO;
        };
    }
}
