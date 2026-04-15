package com.techstore.bank_system.controller;

import com.techstore.bank_system.service.CardEmergencyTokenService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Вариант A: ссылка из письма ведёт на HTML-страницу подтверждения.
 * Далее страница отправляет POST на публичный endpoint /api/public/cards/emergency-block.
 */
@Controller
public class CardEmergencyPageController {

    private final CardEmergencyTokenService tokenService;

    public CardEmergencyPageController(CardEmergencyTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/card-block-confirm")
    public String confirm(@RequestParam("token") String token, Model model) {
        Long cardId = tokenService.validateAndExtractCardId(token);
        model.addAttribute("token", token);
        model.addAttribute("valid", cardId != null);
        model.addAttribute("cardId", cardId);
        return "card-block-confirm";
    }
}

