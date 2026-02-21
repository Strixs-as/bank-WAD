package com.techstore.bank_system.resource;

import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.service.CardService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class CardResource {

    @Autowired
    private CardService cardService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/create/{accountId}")
    public ResponseEntity<?> createCard(@RequestHeader("Authorization") String authHeader, @PathVariable Long accountId) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            Card card = cardService.createCard(userId, accountId);
            return ResponseEntity.status(HttpStatus.CREATED).body(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserCards(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            List<Card> cards = cardService.getUserCards(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getActiveCards(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = extractToken(authHeader);
            Long userId = jwtUtil.extractUserId(token);
            List<Card> cards = cardService.getActiveUserCards(userId);
            return ResponseEntity.ok(cards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> blockCard(@PathVariable Long cardId) {
        try {
            Card card = cardService.blockCard(cardId);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{cardId}/unblock")
    public ResponseEntity<?> unblockCard(@PathVariable Long cardId) {
        try {
            Card card = cardService.unblockCard(cardId);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"message\": \"" + e.getMessage() + "\"}");
        }
    }

    @PutMapping("/{cardId}/deactivate")
    public ResponseEntity<?> deactivateCard(@PathVariable Long cardId) {
        try {
            Card card = cardService.deactivateCard(cardId);
            return ResponseEntity.ok(card);
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
