package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.DepositWithdrawRequest;
import com.techstore.bank_system.dto.TransferRequest;
import com.techstore.bank_system.entity.Transaction;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.TransactionService;
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
@RequestMapping("/api/transactions")
public class TransactionResource {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody TransferRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Transaction transaction = transactionService.transfer(
                    request.getFromAccountNumber(), request.getToAccountNumber(),
                    request.getAmount(), userId, request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DepositWithdrawRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Transaction transaction = transactionService.deposit(
                    request.getAccountNumber(), request.getAmount(), userId, request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody DepositWithdrawRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Transaction transaction = transactionService.withdraw(
                    request.getAccountNumber(), request.getAmount(), userId, request.getDescription());
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<?> getAccountTransactions(@PathVariable Long accountId) {
        try {
            List<Transaction> transactions = transactionService.getAccountTransactions(accountId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(@PathVariable String transactionId) {
        try {
            return transactionService.getTransactionById(transactionId)
                    .map(t -> ResponseEntity.ok((Object) t))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("{\"message\":\"Транзакция не найдена\"}"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private Long resolveUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try { return jwtUtil.extractUserId(authHeader.substring(7)); } catch (Exception ignored) {}
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Optional<User> user = userRepository.findByEmail(auth.getName());
            return user.map(User::getId).orElse(null);
        }
        return null;
    }
}
