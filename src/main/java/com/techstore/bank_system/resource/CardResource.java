package com.techstore.bank_system.resource;

import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.CardService;
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
@RequestMapping("/api/cards")
public class CardResource {

    @Autowired private CardService cardService;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;

    @PostMapping("/create/{accountId}")
    public ResponseEntity<?> createCard(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long accountId) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            Card card = cardService.createCard(userId, accountId);
            return ResponseEntity.status(HttpStatus.CREATED).body(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserCards(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            List<Card> cards = cardService.getUserCards(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCards(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Long userId = resolveUserId(authHeader);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"message\":\"Необходима авторизация\"}");
            List<Card> cards = cardService.getActiveUserCards(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long cardId) {
        try { return ResponseEntity.ok(cardService.blockCard(cardId));
        } catch (Exception e) { return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}"); }
    }

    @PutMapping("/{cardId}/unblock")
    public ResponseEntity<?> unblockCard(@PathVariable Long cardId) {
        try { return ResponseEntity.ok(cardService.unblockCard(cardId));
        } catch (Exception e) { return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}"); }
    }

    @PutMapping("/{cardId}/deactivate")
    public ResponseEntity<?> deactivateCard(@PathVariable Long cardId) {
        try { return ResponseEntity.ok(cardService.deactivateCard(cardId));
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
