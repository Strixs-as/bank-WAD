package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateLoanRequest;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.Loan;
import com.techstore.bank_system.service.LoanService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanResource {

    @Autowired
    private LoanService loanService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createLoan(@RequestHeader("Authorization") String authHeader, @RequestBody CreateLoanRequest request) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            Loan loan = loanService.createLoanApplication(
                    userId,
                    request.getAmount(),
                    request.getDurationMonths(),
                    CurrencyType.valueOf(request.getCurrency()),
                    request.getAccountId()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserLoans(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            List<Loan> loans = loanService.getUserLoans(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{loanId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long loanId) {
        try {
            Loan loan = loanService.approveLoan(loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{loanId}/reject")
    public ResponseEntity<?> rejectLoan(@PathVariable Long loanId) {
        try {
            Loan loan = loanService.rejectLoan(loanId);
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{loanId}/disburse")
    public ResponseEntity<?> disburseLoan(@PathVariable Long loanId) {
        try {
            Loan loan = loanService.disburseLoan(loanId);
            return ResponseEntity.ok(loan);
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
