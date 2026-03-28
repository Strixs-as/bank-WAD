package com.techstore.bank_system.config;

import com.techstore.bank_system.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // ─── Публичные статические страницы
                .requestMatchers(
                    "/", "/index.html", "/login.html", "/register.html",
                    "/dashboard.html", "/deposits.html", "/loans.html", "/javaee.html",
                    "/register", "/login",
                    "/css/**", "/js/**", "/images/**", "/favicon.ico",
                    "/api/auth/**"
                ).permitAll()
                // ─── Java EE Demo
                .requestMatchers("/api/demo/**", "/servlet/**", "/hello").permitAll()
                // ─── Только ADMIN
                .requestMatchers("/admin/**", "/api/admin/**").authenticated()
                // ─── USER или ADMIN
                .requestMatchers("/api/accounts/**", "/api/transactions/**",
                                 "/api/loans/**", "/api/deposits/**").hasAnyRole("USER", "ADMIN")
                // ─── Авторизованные страницы
                .requestMatchers("/home", "/profile").authenticated()
                // ─── Остальные запросы — разрешены (REST API с JWT)
                .anyRequest().permitAll()
            )
            // включаем форму логина (используем страницу /login контроллера)
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // JWT фильтр для API-запросов с Bearer токеном
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
