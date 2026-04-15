package com.techstore.bank_system.controller;

import com.techstore.bank_system.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    /**
     * Тестовый endpoint для проверки почты.
     * POST /email/send
     * JSON: {"to":"gwars274@gmail.com","subject":"Test","text":"Hello"}
     */
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody Map<String, String> body) {
        String to = body.get("to");
        String subject = body.getOrDefault("subject", "Test message");
        String text = body.getOrDefault("text", "Hello from BankSystem");

        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Поле 'to' обязательно"));
        }

        boolean ok = emailService.sendEmail(to, subject, text);
        String status = ok ? "sent" : "skipped_or_failed";

        return ResponseEntity.ok(Map.of(
                "status", status,
                "to", to
        ));
    }
}
