package com.techstore.bank_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model, Authentication auth) {
        model.addAttribute("username", auth != null ? auth.getName() : "guest");
        return "home";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        model.addAttribute("username", auth != null ? auth.getName() : "guest");
        return "profile";
    }
}

