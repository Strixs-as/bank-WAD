package com.techstore.bank_system.config;

import com.techstore.bank_system.service.LoginNotificationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final LoginNotificationService loginNotificationService;

    public LoginSuccessHandler(LoginNotificationService loginNotificationService) {
        this.loginNotificationService = loginNotificationService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String email = authentication != null ? authentication.getName() : null;
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Логин не блокируем, но ошибки отправки письма теперь видны в консоли
        try {
            loginNotificationService.notifyLogin(email, ip, userAgent);
        } catch (Exception e) {
            System.err.println("❌ LoginSuccessHandler: notifyLogin failed for " + email + ": " + e.getMessage());
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
