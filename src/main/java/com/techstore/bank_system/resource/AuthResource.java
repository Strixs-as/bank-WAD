package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.AuthResponse;
import com.techstore.bank_system.dto.LoginRequest;
import com.techstore.bank_system.dto.RegisterRequest;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    /** Количество пользователей — для главной страницы */
    @GetMapping("/count")
    public ResponseEntity<?> getUserCount() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (response.getToken() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", response.getMessage()));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/unlock/request")
    public ResponseEntity<?> requestUnlock(@Valid @RequestBody com.techstore.bank_system.dto.UnlockRequest request,
                                           HttpServletRequest http) {
        try {
            String ip = http.getRemoteAddr();
            String ua = http.getHeader("User-Agent");
            authService.requestUnlockCode(request.getEmail(), ip, ua);
        } catch (Exception ignored) {
        }

        return ResponseEntity.ok(Map.of("message", "Если аккаунт существует, мы отправили код на email"));
    }

    @PostMapping("/unlock/confirm")
    public ResponseEntity<?> confirmUnlock(@Valid @RequestBody com.techstore.bank_system.dto.UnlockConfirmRequest request) {
        try {
            authService.confirmUnlockCode(request.getEmail(), request.getCode());
            return ResponseEntity.ok(Map.of("message", "Аккаунт разблокирован"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка сервера"));
        }
    }
}
