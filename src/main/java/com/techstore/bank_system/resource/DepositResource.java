package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateDepositRequest;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.Deposit;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.DepositService;
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
@RequestMapping("/api/deposits")
public class DepositResource {

    @Autowired private DepositService depositService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createDeposit(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateDepositRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Deposit deposit = depositService.createDeposit(userId, request.getAmount(),
                    request.getDurationMonths(), CurrencyType.valueOf(request.getCurrency()), request.getAccountId());
            return ResponseEntity.status(HttpStatus.CREATED).body(deposit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDeposits(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            List<Deposit> deposits = depositService.getUserDeposits(userId);
            return ResponseEntity.ok(deposits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{depositId}/close")
    public ResponseEntity<?> closeDeposit(@PathVariable Long depositId) {
        try { return ResponseEntity.ok(depositService.closeDeposit(depositId));
        } catch (Exception e) { return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}"); }
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
