package com.techstore.bank_system.controller;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.DepositRepository;
import com.techstore.bank_system.repository.LoanRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final DepositRepository depositRepository;

    public HomeController(UserRepository userRepository,
                          AccountRepository accountRepository,
                          LoanRepository loanRepository,
                          DepositRepository depositRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.depositRepository = depositRepository;
    }

    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        if (auth != null) {
            model.addAttribute("username", auth.getName());
            // Обновляем lastLoginAt
            userRepository.findByEmail(auth.getName()).ifPresent(user -> {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            });
        } else {
            model.addAttribute("username", "guest");
        }
        return "home";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        if (auth != null) {
            String email = auth.getName();
            model.addAttribute("username", email);

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("accounts", accountRepository.findByUserId(user.getId()));
                model.addAttribute("loans", loanRepository.findByUserId(user.getId()));
                model.addAttribute("deposits", depositRepository.findByUserId(user.getId()));
                model.addAttribute("roles", user.getRoles());
            }
        } else {
            model.addAttribute("username", "guest");
        }
        return "profile";
    }

    @GetMapping("/")
    public String index(Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .anyMatch(a -> a.equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin";
            }
            return "redirect:/home";
        }
        return "redirect:/index.html";
    }
}
