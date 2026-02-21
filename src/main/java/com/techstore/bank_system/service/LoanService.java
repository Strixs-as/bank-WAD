package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.LoanRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Loan createLoanApplication(Long userId,
                                     BigDecimal amount,
                                     Integer durationMonths,
                                     CurrencyType currency,
                                     Long accountId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (userOpt.isEmpty() || accountOpt.isEmpty()) {
            throw new RuntimeException("Пользователь или счёт не найдены");
        }

        Loan loan = Loan.builder()
                .loanNumber(generateLoanNumber())
                .principalAmount(amount)
                .remainingAmount(amount)
                .interestRate(new BigDecimal("12.5")) // фиксированная ставка 12.5% годовых
                .durationMonths(durationMonths)
                .currency(currency)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(durationMonths))
                .status(LoanStatus.PENDING)
                .user(userOpt.get())
                .account(accountOpt.get())
                .createdAt(LocalDateTime.now())
                .build();

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approveLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Заявка не найдена");
        }

        Loan loan = loanOpt.get();
        if (!loan.getStatus().equals(LoanStatus.PENDING)) {
            throw new RuntimeException("Заявка уже обработана");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loan.setApprovedAt(LocalDateTime.now());
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan rejectLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Заявка не найдена");
        }

        Loan loan = loanOpt.get();
        loan.setStatus(LoanStatus.REJECTED);
        return loanRepository.save(loan);
    }

    @Transactional
    public Loan disburseLoan(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if (loanOpt.isEmpty()) {
            throw new RuntimeException("Заявка не найдена");
        }

        Loan loan = loanOpt.get();
        if (!loan.getStatus().equals(LoanStatus.APPROVED)) {
            throw new RuntimeException("Заявка должна быть одобрена");
        }

        // Зачисление средств на счёт
        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        account.updateAvailableBalance();
        accountRepository.save(account);

        loan.setStatus(LoanStatus.ACTIVE);
        return loanRepository.save(loan);
    }

    public List<Loan> getUserLoans(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    public BigDecimal calculateMonthlyPayment(Long loanId) {
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        return loanOpt.map(Loan::calculateMonthlyPayment).orElse(BigDecimal.ZERO);
    }

    private String generateLoanNumber() {
        Random random = new Random();
        return "LOAN" + System.currentTimeMillis() + random.nextInt(1000);
    }
}
