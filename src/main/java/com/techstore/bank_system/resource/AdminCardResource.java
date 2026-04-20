package com.techstore.bank_system.resource;

import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Админские операции над картами.
 *
 * ВНИМАНИЕ: legacy-контроллер оставлен только для обратной совместимости.
 * По умолчанию НЕ активен.
 *
 * ВНИМАНИЕ: используем soft-delete (isDeleted=true), физически не удаляем.
 */
@RestController("legacyAdminCardResource")
@org.springframework.context.annotation.Profile("legacy-admin-cards")
@RequestMapping("/api/admin/cards")
public class AdminCardResource {

    private final CardService cardService;

    public AdminCardResource(CardService cardService) {
        this.cardService = cardService;
    }

    public record AdminDeleteCardRequest(String reason) {}

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Card> deleteCard(@PathVariable Long cardId,
                                          @RequestBody(required = false) AdminDeleteCardRequest request) {
        String reason = request != null ? request.reason() : null;
        Card saved = cardService.adminBlockAndDeactivate(cardId, reason);
        return ResponseEntity.ok(saved);
    }
}
