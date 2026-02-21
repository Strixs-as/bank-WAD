package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateDepositRequest;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.Deposit;
import com.techstore.bank_system.service.DepositService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deposits")
public class DepositResource {

    @Autowired
    private DepositService depositService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createDeposit(@RequestHeader("Authorization") String authHeader, @RequestBody CreateDepositRequest request) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            Deposit deposit = depositService.createDeposit(
                    userId,
                    request.getAmount(),
                    request.getDurationMonths(),
                    CurrencyType.valueOf(request.getCurrency()),
                    request.getAccountId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(deposit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserDeposits(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            List<Deposit> deposits = depositService.getUserDeposits(userId);
            return ResponseEntity.ok(deposits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{depositId}/close")
    public ResponseEntity<?> closeDeposit(@PathVariable Long depositId) {
        try {
            Deposit deposit = depositService.closeDeposit(depositId);
            return ResponseEntity.ok(deposit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
}
