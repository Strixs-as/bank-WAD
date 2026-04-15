package com.techstore.bank_system.config;

import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.UserRepository;
import com.techstore.bank_system.service.AccountLockoutService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Обрабатывает провал form-login (/login):
 * - увеличивает счётчик неудачных попыток
 * - при достижении лимита блокирует и отправляет unlock-код на email
 */
@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final AccountLockoutService accountLockoutService;

    public LoginFailureHandler(UserRepository userRepository, AccountLockoutService accountLockoutService) {
        this.userRepository = userRepository;
        this.accountLockoutService = accountLockoutService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String email = request.getParameter("username"); // Spring Security form login: username parameter
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email).ifPresent(user -> handleFailure(user, ip, userAgent));
        }

        // если аккаунт стал заблокированным — покажем это на странице логина
        if (email != null && !email.isBlank()) {
            User u = userRepository.findByEmail(email).orElse(null);
            if (u != null && accountLockoutService.isLocked(u)) {
                // редирект на логин со статусом lock
                getRedirectStrategy().sendRedirect(request, response, "/login?locked");
                return;
            }
        }

        // стандартное поведение: /login?error
        super.onAuthenticationFailure(request, response, exception);
    }

    private void handleFailure(User user, String ip, String userAgent) {
        try {
            accountLockoutService.onLoginFailure(user, ip, userAgent);
        } catch (Exception e) {
            System.err.println("❌ LoginFailureHandler: onLoginFailure failed for " + user.getEmail() + ": " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

