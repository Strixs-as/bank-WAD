package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateAccountRequest;
import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.AccountType;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.service.AccountService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestHeader("Authorization") String authHeader,
                                           @RequestBody CreateAccountRequest request) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);

            AccountType accountType = AccountType.valueOf(request.getAccountType().toUpperCase());

            String currencyRaw = request.getCurrency() != null ? request.getCurrency()
                    : request.getCurrencyStr() != null ? request.getCurrencyStr() : "RUB";
            CurrencyType currency = CurrencyType.valueOf(currencyRaw.toUpperCase());

            Account account = accountService.createAccount(
                    userId,
                    accountType,
                    currency,
                    request.getInitialDeposit()
            );
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
    public ResponseEntity<?> getUserAccounts(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
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
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body((Account) null)); // Simple cast to satisfy type inference if needed, or body(null)
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
}
