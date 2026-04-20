package com.techstore.bank_system.controller;

import com.techstore.bank_system.entity.*;
import com.techstore.bank_system.repository.*;
import com.techstore.bank_system.service.CardService;
import com.techstore.bank_system.application.card.command.AdminDeleteCardHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Set<String> ALLOWED_TABS =
            Set.of("users", "requests", "loans", "deposits", "accounts", "cards");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoanRepository loanRepository;
    private final DepositRepository depositRepository;
    private final AccountRepository accountRepository;
    private final com.techstore.bank_system.service.LoanService loanService;
    private final com.techstore.bank_system.service.DepositService depositService;
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final AdminDeleteCardHandler adminDeleteCardHandler;

    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository,
                           LoanRepository loanRepository,
                           DepositRepository depositRepository,
                           AccountRepository accountRepository,
                           com.techstore.bank_system.service.LoanService loanService,
                           com.techstore.bank_system.service.DepositService depositService,
                           CardService cardService,
                           CardRepository cardRepository,
                           AdminDeleteCardHandler adminDeleteCardHandler) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.loanRepository = loanRepository;
        this.depositRepository = depositRepository;
        this.accountRepository = accountRepository;
        this.loanService = loanService;
        this.depositService = depositService;
        this.cardService = cardService;
        this.cardRepository = cardRepository;
        this.adminDeleteCardHandler = adminDeleteCardHandler;
    }

    @GetMapping
    public String adminHome(Model model) {
        return adminTab("users", model);
    }

    @GetMapping("/tab/{tab}")
    public String adminTab(@PathVariable String tab, Model model) {
        String safeTab = ALLOWED_TABS.contains(tab) ? tab : "users";

        List<User> users = userRepository.findAll();
        List<Loan> loans = loanRepository.findAll();
        List<Deposit> deposits = depositRepository.findAll();
        List<Account> accounts = accountRepository.findAll();
        // В админке показываем все карты, включая soft-deleted (для аудита и управления).
        List<Card> cards = cardRepository.findAdminAllCardsIncludingDeleted();
        List<Loan> pendingLoans = loanRepository.findByStatus(LoanStatus.PENDING);
        List<Deposit> pendingDeposits = depositRepository.findByStatus(DepositStatus.PENDING);

        model.addAttribute("users", users);
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("loans", loans);
        model.addAttribute("deposits", deposits);
        model.addAttribute("accounts", accounts);
        model.addAttribute("cards", cards);
        model.addAttribute("pendingLoans", pendingLoans);
        model.addAttribute("pendingDeposits", pendingDeposits);
        model.addAttribute("tab", safeTab);

        model.addAttribute("totalPending", pendingLoans.size() + pendingDeposits.size());
        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalLoans", loans.size());
        model.addAttribute("totalDeposits", deposits.size());
        model.addAttribute("totalAccounts", accounts.size());
        model.addAttribute("totalCards", cards.size());

        return "admin";
    }

    @ResponseBody
    @GetMapping("/api/stats")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(Map.of(
                "users", userRepository.count(),
                "loans", loanRepository.count(),
                "deposits", depositRepository.count(),
                "accounts", accountRepository.count(),
                "pendingLoans", loanRepository.findByStatus(LoanStatus.PENDING).size(),
                "pendingDeposits", depositRepository.findByStatus(DepositStatus.PENDING).size()
        ));
    }

    @ResponseBody
    @GetMapping("/api/user/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            List<Loan> userLoans = loanRepository.findByUserId(id);
            List<Deposit> userDeposits = depositRepository.findByUserId(id);
            List<Account> userAccounts = accountRepository.findByUserId(id);

            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("firstName", user.getFirstName());
            data.put("lastName", user.getLastName());
            data.put("email", user.getEmail());
            data.put("phone", user.getPhoneNumber());
            data.put("passport", user.getPassportNumber());
            data.put("address", user.getAddress());
            data.put("dateOfBirth", user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
            data.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
            data.put("lastLoginAt", user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null);
            data.put("isActive", user.getIsActive());
            data.put("isVerified", user.getIsVerified());
            data.put("roles", user.getRoles().stream().map(Role::getName).toList());

            data.put("accounts", userAccounts.stream().map(a -> Map.of(
                    "id", a.getId(),
                    "number", String.valueOf(a.getAccountNumber()),
                    "type", String.valueOf(a.getAccountType()),
                    "balance", String.valueOf(a.getBalance()),
                    "currency", String.valueOf(a.getCurrency()),
                    "active", a.getIsActive()
            )).toList());

            data.put("loans", userLoans.stream().map(l -> Map.of(
                    "id", l.getId(),
                    "number", String.valueOf(l.getLoanNumber()),
                    "amount", String.valueOf(l.getPrincipalAmount()),
                    "currency", String.valueOf(l.getCurrency()),
                    "status", String.valueOf(l.getStatus()),
                    "rate", String.valueOf(l.getInterestRate()),
                    "months", l.getDurationMonths()
            )).toList());

            data.put("deposits", userDeposits.stream().map(d -> Map.of(
                    "id", d.getId(),
                    "number", String.valueOf(d.getDepositNumber()),
                    "amount", String.valueOf(d.getAmount()),
                    "currency", String.valueOf(d.getCurrency()),
                    "status", String.valueOf(d.getStatus()),
                    "rate", String.valueOf(d.getInterestRate()),
                    "months", d.getDurationMonths()
            )).toList());

            return ResponseEntity.ok(data);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        String currentEmail = authentication != null ? authentication.getName() : null;

        userRepository.findById(id).ifPresent(user -> {
            if (currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail())) {
                return;
            }

            // 1) Удаляем кредиты и депозиты пользователя (они ссылаются на account_id NOT NULL)
            loanRepository.findAll().stream()
                    .filter(l -> l.getUser() != null && Objects.equals(l.getUser().getId(), user.getId()))
                    .forEach(loanRepository::delete);

            depositRepository.findAll().stream()
                    .filter(d -> d.getUser() != null && Objects.equals(d.getUser().getId(), user.getId()))
                    .forEach(depositRepository::delete);

            // 2) Удаляем счета пользователя (и связанные с ними транзакции/карты)
            accountRepository.findAll().stream()
                    .filter(a -> a.getUser() != null && Objects.equals(a.getUser().getId(), user.getId()))
                    .forEach(acc -> {
                        // транзакции
                        if (acc.getSentTransactions() != null) {
                            acc.getSentTransactions().clear();
                        }
                        if (acc.getReceivedTransactions() != null) {
                            acc.getReceivedTransactions().clear();
                        }
                        // карты
                        if (acc.getCards() != null) {
                            acc.getCards().clear();
                        }
                        accountRepository.delete(acc);
                    });

            // 3) Удаляем самого пользователя
            userRepository.delete(user);
        });

        return "redirect:/admin/tab/users";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable Long id, Authentication authentication) {
        String currentEmail = authentication != null ? authentication.getName() : null;

        userRepository.findById(id).ifPresent(user -> {
            if (currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail())) {
                return;
            }
            user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
            userRepository.save(user);
        });

        return "redirect:/admin/tab/users";
    }

    @PostMapping("/set-role/{id}")
    public String setRole(@PathVariable Long id,
                          @RequestParam("role") String roleName,
                          Authentication authentication) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            return "redirect:/admin/tab/users";
        }

        String currentEmail = authentication != null ? authentication.getName() : null;

        userRepository.findById(id).ifPresent(user -> {
            boolean isSelf = currentEmail != null && currentEmail.equalsIgnoreCase(user.getEmail());

            if (isSelf && !"ADMIN".equalsIgnoreCase(roleName)) {
                return;
            }

            user.getRoles().clear();
            user.getRoles().add(roleOpt.get());
            userRepository.save(user);
        });

        return "redirect:/admin/tab/users";
    }

    @PostMapping("/loan/approve/{id}")
    public String approveLoan(@PathVariable Long id) {
        try {
            loanService.approveLoan(id);
        } catch (Exception e) {
            System.err.println("Error approving loan: " + e.getMessage());
        }
        return "redirect:/admin/tab/requests";
    }

    @PostMapping("/loan/reject/{id}")
    public String rejectLoan(@PathVariable Long id) {
        try {
            loanService.rejectLoan(id);
        } catch (Exception e) {
            System.err.println("Error rejecting loan: " + e.getMessage());
        }
        return "redirect:/admin/tab/requests";
    }

    @PostMapping("/loan/close/{id}")
    public String closeLoan(@PathVariable Long id) {
        loanRepository.findById(id).ifPresent(loan -> {
            if (loan.getStatus() != LoanStatus.ACTIVE) {
                return;
            }
            loan.setStatus(LoanStatus.CLOSED);
            loan.setClosedAt(LocalDateTime.now());
            loanRepository.save(loan);
        });
        return "redirect:/admin/tab/loans";
    }

    @PostMapping("/loan/delete/{id}")
    public String deleteLoan(@PathVariable Long id) {
        loanRepository.findById(id).ifPresent(loanRepository::delete);
        return "redirect:/admin/tab/loans";
    }

    @PostMapping("/deposit/approve/{id}")
    public String approveDeposit(@PathVariable Long id) {
        try {
            depositService.approveDeposit(id);
        } catch (Exception e) {
            System.err.println("Error approving deposit: " + e.getMessage());
        }
        return "redirect:/admin/tab/requests";
    }

    @PostMapping("/deposit/reject/{id}")
    public String rejectDeposit(@PathVariable Long id) {
        try {
            depositService.rejectDeposit(id);
        } catch (Exception e) {
            System.err.println("Error rejecting deposit: " + e.getMessage());
        }
        return "redirect:/admin/tab/requests";
    }

    @PostMapping("/deposit/close/{id}")
    public String closeDeposit(@PathVariable Long id) {
        depositRepository.findById(id).ifPresent(deposit -> {
            if (deposit.getStatus() != DepositStatus.ACTIVE) {
                return;
            }
            deposit.setStatus(DepositStatus.CLOSED);
            deposit.setClosedAt(LocalDateTime.now());
            depositRepository.save(deposit);
        });
        return "redirect:/admin/tab/deposits";
    }

    @PostMapping("/deposit/delete/{id}")
    public String deleteDeposit(@PathVariable Long id) {
        depositRepository.findById(id).ifPresent(depositRepository::delete);
        return "redirect:/admin/tab/deposits";
    }

    @PostMapping("/account/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        // Удаляем связанные сущности (иначе SQL Server может ругаться на FK)
        accountRepository.findById(id).ifPresent(acc -> {
            // 1) Транзакции
            if (acc.getSentTransactions() != null) {
                acc.getSentTransactions().forEach(t -> {
                    try { t.setFromAccount(null); } catch (Exception ignored) {}
                });
                acc.getSentTransactions().clear();
            }
            if (acc.getReceivedTransactions() != null) {
                acc.getReceivedTransactions().forEach(t -> {
                    try { t.setToAccount(null); } catch (Exception ignored) {}
                });
                acc.getReceivedTransactions().clear();
            }

            // 2) Карты (orphanRemoval не включен, но cascade=ALL есть) — удалим явно
            if (acc.getCards() != null) {
                acc.getCards().clear();
            }

            // 3) Кредиты/депозиты
            loanRepository.findAll().stream()
                    .filter(l -> l.getAccount() != null && Objects.equals(l.getAccount().getId(), acc.getId()))
                    .forEach(loanRepository::delete);

            depositRepository.findAll().stream()
                    .filter(d -> d.getAccount() != null && Objects.equals(d.getAccount().getId(), acc.getId()))
                    .forEach(depositRepository::delete);

            // 4) Удаляем счёт
            accountRepository.delete(acc);
        });

        return "redirect:/admin/tab/accounts";
    }

    @PostMapping("/card/delete/{id}")
    public String adminDeleteCard(@PathVariable("id") Long cardId,
                                  @RequestParam(value = "reason", required = false) String reason,
                                  Authentication authentication) {
        try {
            String performedBy = (authentication != null) ? authentication.getName() : "admin";
            adminDeleteCardHandler.handle(new com.techstore.bank_system.application.card.command.AdminDeleteCardCommand(
                    cardId,
                    reason,
                    performedBy
            ));
        } catch (Exception e) {
            System.err.println("Admin: failed to delete card id=" + cardId + " :: " + e.getMessage());
        }
        return "redirect:/admin/tab/cards";
    }
}
