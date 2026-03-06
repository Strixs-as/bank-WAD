package com.techstore.bank_system.controller;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult br) {
        if (br.hasErrors()) {
            return "register";
        }

        // validate uniqueness to avoid DB constraint errors
        if (user.getEmail() != null && userRepository.findByEmail(user.getEmail()).isPresent()) {
            br.rejectValue("email", "error.user", "Пользователь с таким email уже существует");
            return "register";
        }
        if (user.getPassportNumber() != null && userRepository.findByPassportNumber(user.getPassportNumber()).isPresent()) {
            br.rejectValue("passportNumber", "error.user", "Пользователь с таким номером паспорта уже существует");
            return "register";
        }
        if (user.getPhoneNumber() != null && userRepository.findByPhoneNumber(user.getPhoneNumber()).isPresent()) {
            br.rejectValue("phoneNumber", "error.user", "Пользователь с таким номером телефона уже существует");
            return "register";
        }

        // hash password and save
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }
}
