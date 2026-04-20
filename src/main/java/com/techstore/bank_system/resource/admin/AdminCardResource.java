package com.techstore.bank_system.resource.admin;

import com.techstore.bank_system.application.card.command.AdminDeleteCardCommand;
import com.techstore.bank_system.application.card.command.AdminDeleteCardHandler;
import com.techstore.bank_system.application.card.command.AdminDeleteCardResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API: операции с картами.
 */
@RestController("adminCardResourceV2")
@RequestMapping("/api/admin/cards")
public class AdminCardResource {
    private final AdminDeleteCardHandler deleteHandler;

    public AdminCardResource(AdminDeleteCardHandler deleteHandler) {
        this.deleteHandler = deleteHandler;
    }

    public record DeleteCardRequest(String reason) {}

    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminDeleteCardResult> delete(@PathVariable Long cardId,
                                                       @RequestBody(required = false) DeleteCardRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String performedBy = (auth != null && auth.getName() != null) ? auth.getName() : "admin";
        String reason = (req != null) ? req.reason() : null;

        AdminDeleteCardResult result = deleteHandler.handle(new AdminDeleteCardCommand(cardId, reason, performedBy));
        return ResponseEntity.ok(result);
    }
}
