package com.techstore.bank_system.controller;

import com.techstore.bank_system.entity.Role;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.RoleRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "patronymic", required = false) String patronymic,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "passportNumber", required = false) String passportNumber,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "address", required = false) String address,
            Model model) {

        // Проверка обязательных полей
        if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()
                || email == null || email.isBlank() || password == null || password.isBlank()) {
            model.addAttribute("error", "Имя, фамилия, email и пароль обязательны");
            model.addAttribute("user", new User());
            return "register";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Пароль должен быть не менее 6 символов");
            model.addAttribute("user", new User());
            return "register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Пользователь с таким email уже существует");
            model.addAttribute("user", new User());
            return "register";
        }

        // Проверка уникальности phoneNumber
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                model.addAttribute("error", "Пользователь с таким номером телефона уже существует");
                model.addAttribute("user", new User());
                return "register";
            }
        }

        // Проверка уникальности passportNumber
        if (passportNumber != null && !passportNumber.isBlank()) {
            if (userRepository.findByPassportNumber(passportNumber).isPresent()) {
                model.addAttribute("error", "Пользователь с таким номером паспорта уже существует");
                model.addAttribute("user", new User());
                return "register";
            }
        }

        // Получаем роль USER
        Role userRole = roleRepository.findByName("USER").orElse(null);

        User newUser = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .patronymic(patronymic != null && !patronymic.isBlank() ? patronymic : null)
                .email(email)
                .password(passwordEncoder.encode(password))
                .phoneNumber(phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : null)
                .passportNumber(passportNumber != null && !passportNumber.isBlank() ? passportNumber : null)
                .dateOfBirth(dateOfBirth != null && !dateOfBirth.isBlank() ? LocalDate.parse(dateOfBirth) : null)
                .address(address != null && !address.isBlank() ? address : null)
                .isActive(true)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        if (userRole != null) {
            newUser.getRoles().add(userRole);
        }

        userRepository.save(newUser);
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }
}


