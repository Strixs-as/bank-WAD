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

    @Autowired
    private EmailService emailService;

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
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Кредит не найден"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Кредит уже обработан");
        }

        loan.setStatus(LoanStatus.APPROVED);
        // Зачисляем средства на счет
        Account account = loan.getAccount();
        account.setBalance(account.getBalance().add(loan.getPrincipalAmount()));
        accountRepository.save(account);

        loanRepository.save(loan);

        if (loan.getUser() != null && loan.getUser().getEmail() != null) {
            emailService.sendEmail(
                    loan.getUser().getEmail(),
                    "Кредит одобрен / Loan Approved",
                    "Ваша заявка на кредит на сумму " + loan.getPrincipalAmount() + " была одобрена.\nYour loan application for " + loan.getPrincipalAmount() + " has been approved."
            );
        }

        return loan;
    }

    public Loan rejectLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Кредит не найден"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Кредит уже обработан");
        }

        loan.setStatus(LoanStatus.REJECTED);
        loanRepository.save(loan);

        if (loan.getUser() != null && loan.getUser().getEmail() != null) {
            emailService.sendEmail(
                    loan.getUser().getEmail(),
                    "Кредит отклонен / Loan Rejected",
                    "К сожалению, ваша заявка на кредит была отклонена.\nUnfortunately, your loan application has been rejected."
            );
        }

        return loan;
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
