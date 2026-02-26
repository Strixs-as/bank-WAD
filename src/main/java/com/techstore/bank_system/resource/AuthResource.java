package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.AuthResponse;
import com.techstore.bank_system.dto.LoginRequest;
import com.techstore.bank_system.dto.RegisterRequest;
import com.techstore.bank_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthResource {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody(required = false) RegisterRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Тело запроса пустое. Отправьте JSON: {firstName, lastName, email, password}"));
            }
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле email обязательно"));
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле password обязательно"));
            }
            if (request.getFirstName() == null || request.getFirstName().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле firstName обязательно"));
            }
            if (request.getLastName() == null || request.getLastName().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле lastName обязательно"));
            }

            AuthResponse response = authService.register(request);
            if (response.getToken() == null) {
                return ResponseEntity.badRequest().body(Map.of("message", response.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при регистрации: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(Map.of("message",
                        "Тело запроса пустое. Отправьте JSON: {email, password}"));
            }
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле email обязательно"));
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Поле password обязательно"));
            }

            AuthResponse response = authService.login(request);
            if (response.getToken() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", response.getMessage()));
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Ошибка при входе: " + e.getMessage()));
        }
    }
}

