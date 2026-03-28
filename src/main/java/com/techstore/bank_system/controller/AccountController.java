package com.techstore.bank_system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import java.security.Principal;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AccountService;
import com.techstore.bank_system.service.LoanService;
import com.techstore.bank_system.service.DepositService;
import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.Loan;
import com.techstore.bank_system.entity.Deposit;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final LoanService loanService;
    private final DepositService depositService;
    private final UserRepository userRepository;

    @GetMapping("/accounts")
    public String getAllAccounts(Model model) {
        // model.addAttribute("accounts", accountService.getAllAccounts());  // Replaced with specific user accounts below
        return "accounts";
    }

    @PostMapping("/deactivate/{id}")
    public String deactivateAccount(@PathVariable Long id, Principal principal) {
        accountService.closeAccount(id); // Using the actual method
        return "redirect:/accounts";
    }

    // New profile method with full details
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);

            User user = userRepository.findByEmail(username).orElse(null);
            if (user != null) {
                model.addAttribute("user", user);
                model.addAttribute("roles", user.getRoles());

                List<Account> accounts = accountService.getUserAccounts(user.getId());
                model.addAttribute("accounts", accounts);

                List<Loan> loans = loanService.getUserLoans(user.getId());
                model.addAttribute("loans", loans);

                List<Deposit> deposits = depositService.getUserDeposits(user.getId());
                model.addAttribute("deposits", deposits);
            }
        }
        return "profile";
    }
}
