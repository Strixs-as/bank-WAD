package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateAccountRequest;
import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.AccountType;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AccountService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounts")
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createAccount(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateAccountRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"message\": \"Необходима авторизация\"}");
            }

            AccountType accountType = AccountType.valueOf(request.getAccountType().toUpperCase());

            String currencyRaw = request.getCurrency() != null ? request.getCurrency()
                    : request.getCurrencyStr() != null ? request.getCurrencyStr() : "RUB";
            CurrencyType currency = CurrencyType.valueOf(currencyRaw.toUpperCase());

            Account account = accountService.createAccount(userId, accountType, currency, request.getInitialDeposit());
            return ResponseEntity.status(HttpStatus.CREATED).body(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("{\"message\": \"Неверный тип счета или валюта: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserAccounts(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("{\"message\": \"Необходима авторизация\"}");
            }
            List<Account> accounts = accountService.getUserAccounts(userId);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(@PathVariable String accountNumber) {
        try {
            return accountService.getAccountByNumber(accountNumber)
                    .map(a -> ResponseEntity.ok((Object) a))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"message\": \"Счёт не найден\"}"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Определяет userId: сначала из JWT, потом из Spring Security сессии.
     */
    private Long resolveUserId(String authHeader) {
        // 1. Попытка из JWT
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                return jwtUtil.extractUserId(token);
            } catch (Exception ignored) {}
        }
        // 2. Fallback на Spring Security сессию
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            Optional<User> user = userRepository.findByEmail(email);
            return user.map(User::getId).orElse(null);
        }
        return null;
    }
}
