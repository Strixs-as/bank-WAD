package com.techstore.bank_system.config;

import com.techstore.bank_system.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // ─── Публичные эндпоинты (Java EE тақырыбы #11 — permitAll)
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/register.html",
                    "/dashboard.html", "/deposits.html", "/loans.html",
                    "/register", "/login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/api/auth/**"
                ).permitAll()
                // ─── Java EE Demo (Servlet + REST demo — барлығына ашық)
                .requestMatchers("/api/demo/**", "/servlet/**").permitAll()
                // ─── Тек ADMIN рөлі үшін (Java EE тақырыбы #11 — ROLE_ADMIN)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // ─── Тек USER немесе ADMIN рөлі үшін (Java EE тақырыбы #11 — ROLE_USER)
                .requestMatchers("/api/accounts/**", "/api/transactions/**",
                                 "/api/loans/**", "/api/deposits/**").hasAnyRole("USER", "ADMIN")
                // ─── Қалған сұраулар JWT фильтрі арқылы тексеріледі
                .anyRequest().permitAll()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

