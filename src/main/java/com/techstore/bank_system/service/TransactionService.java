package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.TransactionRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Transaction transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, Long initiatedByUserId, String description) {
        // Проверка счетов
        Optional<Account> fromAccountOpt = accountRepository.findByAccountNumber(fromAccountNumber);
        Optional<Account> toAccountOpt = accountRepository.findByAccountNumber(toAccountNumber);

        if (fromAccountOpt.isEmpty()) {
            throw new RuntimeException("Счет отправителя не найден");
        }

        if (toAccountOpt.isEmpty()) {
            throw new RuntimeException("Счет получателя не найден");
        }

        Account fromAccount = fromAccountOpt.get();
        Account toAccount = toAccountOpt.get();

        // Проверки валидности
        if (!fromAccount.getIsActive() || fromAccount.getIsFrozen()) {
            throw new RuntimeException("Счет отправителя неактивен или заморожен");
        }

        if (!toAccount.getIsActive() || toAccount.getIsFrozen()) {
            throw new RuntimeException("Счет получателя неактивен или заморожен");
        }

        if (fromAccount.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств на счете");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма перевода должна быть больше нуля");
        }

        // Вычисление комиссии (0.5% для переводов)
        BigDecimal fee = amount.multiply(new BigDecimal("0.005"));
        BigDecimal totalAmount = amount.add(fee);

        if (fromAccount.getAvailableBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Недостаточно средств для покрытия комиссии");
        }

        // Получение пользователя
        Optional<User> initiatedBy = userRepository.findById(initiatedByUserId);

        // Создание транзакции
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(TransactionType.TRANSFER)
                .amount(amount)
                .currency(fromAccount.getCurrency())
                .fee(fee)
                .status(TransactionStatus.PROCESSING)
                .description(description)
                .createdAt(LocalDateTime.now())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .initiatedBy(initiatedBy.orElse(null))
                .build();

        try {
            // Списание со счета отправителя
            fromAccount.setBalance(fromAccount.getBalance().subtract(totalAmount));
            fromAccount.updateAvailableBalance();

            // Зачисление на счет получателя
            toAccount.setBalance(toAccount.getBalance().add(amount));
            toAccount.updateAvailableBalance();

            // Обновление статуса транзакции
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            // Сохранение изменений
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Ошибка при выполнении транзакции: " + e.getMessage());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction deposit(String accountNumber, BigDecimal amount, Long initiatedByUserId, String description) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счет не найден");
        }

        Account account = accountOpt.get();

        if (!account.getIsActive() || account.getIsFrozen()) {
            throw new RuntimeException("Счет неактивен или заморожен");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма пополнения должна быть больше нуля");
        }

        Optional<User> initiatedBy = userRepository.findById(initiatedByUserId);

        // Создание транзакции
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .currency(account.getCurrency())
                .fee(BigDecimal.ZERO)
                .status(TransactionStatus.PROCESSING)
                .description(description)
                .createdAt(LocalDateTime.now())
                .toAccount(account)
                .initiatedBy(initiatedBy.orElse(null))
                .build();

        try {
            // Зачисление на счет
            account.setBalance(account.getBalance().add(amount));
            account.updateAvailableBalance();

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            accountRepository.save(account);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Ошибка при пополнении: " + e.getMessage());
        }

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction withdraw(String accountNumber, BigDecimal amount, Long initiatedByUserId, String description) {
        Optional<Account> accountOpt = accountRepository.findByAccountNumber(accountNumber);

        if (accountOpt.isEmpty()) {
            throw new RuntimeException("Счет не найден");
        }

        Account account = accountOpt.get();

        if (!account.getIsActive() || account.getIsFrozen()) {
            throw new RuntimeException("Счет неактивен или заморожен");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма снятия должна быть больше нуля");
        }

        if (account.getAvailableBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств на счете");
        }

        Optional<User> initiatedBy = userRepository.findById(initiatedByUserId);

        // Создание транзакции
        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(TransactionType.WITHDRAWAL)
                .amount(amount)
                .currency(account.getCurrency())
                .fee(BigDecimal.ZERO)
                .status(TransactionStatus.PROCESSING)
                .description(description)
                .createdAt(LocalDateTime.now())
                .fromAccount(account)
                .initiatedBy(initiatedBy.orElse(null))
                .build();

        try {
            // Списание со счета
            account.setBalance(account.getBalance().subtract(amount));
            account.updateAvailableBalance();

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            accountRepository.save(account);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            throw new RuntimeException("Ошибка при снятии: " + e.getMessage());
        }

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(Long accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    public List<Transaction> getAccountTransactionsByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    public Optional<Transaction> getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }
}

