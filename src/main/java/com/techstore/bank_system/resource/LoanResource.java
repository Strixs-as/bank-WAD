package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.CreateLoanRequest;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.entity.Loan;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.LoanService;
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
@RequestMapping("/api/loans")
public class LoanResource {

    @Autowired private LoanService loanService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createLoan(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateLoanRequest request) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Loan loan = loanService.createLoanApplication(userId, request.getAmount(),
                    request.getDurationMonths(), CurrencyType.valueOf(request.getCurrency()), request.getAccountId());
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserLoans(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            List<Loan> loans = loanService.getUserLoans(userId);
            return ResponseEntity.ok(loans);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{loanId}/approve")
    public ResponseEntity<?> approveLoan(@PathVariable Long loanId) {
        try { return ResponseEntity.ok(loanService.approveLoan(loanId));
        } catch (Exception e) { return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}"); }
    }

    @PutMapping("/{loanId}/reject")
    public ResponseEntity<?> rejectLoan(@PathVariable Long loanId) {
        try { return ResponseEntity.ok(loanService.rejectLoan(loanId));
        } catch (Exception e) { return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}"); }
    }

    @PutMapping("/{loanId}/disburse")
    public ResponseEntity<?> disburseLoan(@PathVariable Long loanId) {
        try { return ResponseEntity.ok(loanService.disburseLoan(loanId));
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
