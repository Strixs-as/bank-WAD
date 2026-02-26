package com.techstore.bank_system.service;

import com.techstore.bank_system.dto.AuthResponse;
import com.techstore.bank_system.dto.LoginRequest;
import com.techstore.bank_system.dto.RegisterRequest;
import com.techstore.bank_system.entity.Role;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.RoleRepository;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.util.JwtUtil;
import com.techstore.bank_system.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        // Проверка существующего пользователя по email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return AuthResponse.builder()
                    .message("Пользователь с таким email уже существует")
                    .build();
        }

        // Автозаполнение необязательных полей дефолтными значениями
        String phone = request.getPhoneNumber() != null ? request.getPhoneNumber() : ("+7" + System.currentTimeMillis() % 10000000000L);
        String passport = request.getPassportNumber() != null ? request.getPassportNumber() : String.valueOf(System.currentTimeMillis() % 10000000000L);
        LocalDate dob = request.getDateOfBirth() != null ? request.getDateOfBirth() : LocalDate.of(2000, 1, 1);
        String address = request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress() : "Не указан";

        // Проверка дубликатов phoneNumber и passportNumber
        if (userRepository.findByPhoneNumber(phone).isPresent()) {
            return AuthResponse.builder()
                    .message("Пользователь с таким номером телефона уже существует")
                    .build();
        }
        if (userRepository.findByPassportNumber(passport).isPresent()) {
            return AuthResponse.builder()
                    .message("Пользователь с таким номером паспорта уже существует")
                    .build();
        }

        // Создание нового пользователя
        User user = User.builder()
                .email(request.getEmail())
                .password(PasswordUtil.hashPassword(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .patronymic(request.getPatronymic())
                .phoneNumber(phone)
                .passportNumber(passport)
                .dateOfBirth(dob)
                .address(address)
                .isActive(true)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        // Назначение роли USER по умолчанию
        Optional<Role> userRole = roleRepository.findByName("USER");
        if (userRole.isEmpty()) {
            Role newRole = Role.builder()
                    .name("USER")
                    .description("Обычный пользователь")
                    .build();
            roleRepository.save(newRole);
            user.getRoles().add(newRole);
        } else {
            user.getRoles().add(userRole.get());
        }

        User savedUser = userRepository.save(user);

        // Создание JWT-токена
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                "USER"
        );

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFirstName() + " " + savedUser.getLastName())
                .role("USER")
                .message("Регистрация успешна")
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return AuthResponse.builder()
                    .message("Неверный email или пароль")
                    .build();
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .message("Неверный email или пароль")
                    .build();
        }

        if (!user.getIsActive()) {
            return AuthResponse.builder()
                    .message("Ваш аккаунт деактивирован")
                    .build();
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String role = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next().getName();
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), role);

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .role(role)
                .message("Вход выполнен")
                .build();
    }
}
