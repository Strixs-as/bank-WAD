package com.techstore.bank_system.config;

import com.techstore.bank_system.service.CustomUserDetailsService;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.time.LocalDateTime;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @RestController
    class DebugSecurityController {
        @GetMapping("/api/auth/roles")
        public Object getRoles() {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return "No authentication";
            return java.util.Map.of("name", auth.getName(), "authorities", auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        }
    }

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final UserRepository userRepository;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          LoginSuccessHandler loginSuccessHandler,
                          LoginFailureHandler loginFailureHandler,
                          UserRepository userRepository) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.loginSuccessHandler = loginSuccessHandler;
        this.loginFailureHandler = loginFailureHandler;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                new AntPathRequestMatcher("/api/**"),
                                new AntPathRequestMatcher("/servlet/**"),
                                new AntPathRequestMatcher("/hello"),
                                // фронт делает POST /logout без CSRF-токена
                                new AntPathRequestMatcher("/logout", "POST")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index.html", "/login.html", "/register.html",
                                "/dashboard.html", "/deposits.html", "/loans.html", "/javaee.html",
                                "/card-3d.html", "/unlock.html",
                                "/register", "/login",
                                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                                "/api/auth/**",
                                "/api/public/cards/**"
                        ).permitAll()
                        .requestMatchers("/api/demo/**", "/servlet/**", "/hello").permitAll()
                        // logout должен быть POST и обрабатываться Spring Security
                        .requestMatchers(new AntPathRequestMatcher("/logout", "POST")).permitAll()
                        .requestMatchers("/admin", "/admin/**", "/api/admin", "/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/analytics/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(
                                "/api/accounts/**", "/api/transactions/**",
                                "/api/loans/**", "/api/deposits/**"
                        ).hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/home", "/profile").authenticated()
                        // важно: anyRequest должен быть последним
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                        .accessDeniedHandler(adminAccessDeniedHandler())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .defaultSuccessUrl("/home", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "POST"))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessUrl("/index.html")
                        .permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler adminAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) -> {
            String uri = request.getRequestURI();
            if (uri != null && uri.startsWith("/admin")) {
                response.sendRedirect("/home?error=admin");
                return;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        };
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

        // Проверка блокировки аккаунта до аутентификации (для form-login)
        provider.setPreAuthenticationChecks(new UserDetailsChecker() {
            @Override
            public void check(UserDetails user) {
                // базовые проверки (enabled, accountNonExpired, credentialsNonExpired, accountNonLocked)
                new org.springframework.security.authentication.AccountStatusUserDetailsChecker().check(user);

                // наша проверка lockoutUntil в БД
                userRepository.findByEmail(user.getUsername()).ifPresent(u -> {
                    if (u.getLockoutUntil() != null && u.getLockoutUntil().isAfter(LocalDateTime.now())) {
                        throw new LockedException("Account locked until " + u.getLockoutUntil());
                    }
                });
            }
        });

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
