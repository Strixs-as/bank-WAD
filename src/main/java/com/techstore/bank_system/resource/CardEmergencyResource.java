package com.techstore.bank_system.resource;

import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.service.CardEmergencyTokenService;
import com.techstore.bank_system.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Экстренная блокировка карты по ссылке из email.
 *
 * Важно: endpoint публичный (permitAll), но защищён одноразовым токеном (HMAC + TTL).
 */
@RestController
@RequestMapping("/api/public/cards")
public class CardEmergencyResource {

    private final CardEmergencyTokenService tokenService;
    private final CardService cardService;

    public CardEmergencyResource(CardEmergencyTokenService tokenService, CardService cardService) {
        this.tokenService = tokenService;
        this.cardService = cardService;
    }

    @PostMapping("/emergency-block")
    public ResponseEntity<?> emergencyBlock(@RequestParam("token") String token) {
        Long cardId = tokenService.validateAndExtractCardId(token);
        if (cardId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Ссылка недействительна или истекла"));
        }

        try {
            Card blocked = cardService.blockCard(cardId);
            return ResponseEntity.ok(Map.of(
                    "message", "Карта заблокирована",
                    "cardId", blocked.getId(),
                    "blockedAt", blocked.getBlockedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}

