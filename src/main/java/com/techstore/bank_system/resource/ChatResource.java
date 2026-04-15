package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.ChatRequest;
import com.techstore.bank_system.dto.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatResource {

    @PostMapping
    public ResponseEntity<?> chat(@Valid @RequestBody ChatRequest request,
                                  Authentication authentication) {
        // Простой, но полезный «банковский» бот без внешних API.
        // Можно заменить на LLM позже, не меняя контракт.

        String user = authentication != null ? authentication.getName() : "";
        String msg = normalize(request.getMessage());

        ChatResponse response = new ChatResponse();

        if (msg.isBlank()) {
            response.setReply("Напишите ваш вопрос, и я помогу.");
            response.setQuickReplies(List.of("Мои счета", "Перевод", "Кредиты", "Вклады"));
            return ResponseEntity.ok(response);
        }

        // быстрые сценарии
        if (containsAny(msg, "привет", "здрав", "hello", "салам", "сәлем")) {
            response.setReply("Здравствуйте" + (user.isBlank() ? "!" : ", " + user + "!") + " Я виртуальный помощник BankSystem. Чем помочь?");
            response.setQuickReplies(List.of("Мои счета", "Как сделать перевод?", "Забыли пароль", "Админ панель"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "счет", "счёт", "account", "баланс")) {
            response.setReply("Счета находятся в разделе «Мои счета». Там вы увидите баланс и валюту. Хотите открыть новый счёт?");
            response.setQuickReplies(List.of("Открыть счёт", "История операций"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "перевод", "transfer")) {
            response.setReply("Чтобы сделать перевод: откройте «Переводы» → выберите счёт отправителя → укажите номер счёта получателя и сумму → нажмите «Отправить»." );
            response.setQuickReplies(List.of("Комиссия", "Лимиты", "История"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "кредит", "loan")) {
            response.setReply("Кредиты доступны в разделе «Кредиты». Вы можете подать заявку, а администратор её одобрит или отклонит.");
            response.setQuickReplies(List.of("Открыть кредиты", "Статусы заявок"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "вклад", "депозит", "deposit")) {
            response.setReply("Вклады доступны в разделе «Вклады». После подачи заявки администратор может её одобрить.");
            response.setQuickReplies(List.of("Открыть вклады", "Проценты"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "забыл", "забыли", "пароль", "unlock", "сброс")) {
            response.setReply("Для восстановления: нажмите «Забыли пароль?» на странице входа. Мы отправим код на email, чтобы разблокировать аккаунт/сбросить пароль.");
            response.setQuickReplies(List.of("Открыть восстановление", "Как работает код"));
            return ResponseEntity.ok(response);
        }

        if (containsAny(msg, "админ", "admin")) {
            response.setReply("Админ панель доступна только пользователям с ролью ADMIN. Если вы вошли как администратор — ссылка появится в меню кабинета.");
            response.setQuickReplies(List.of("Как войти админом", "Почему 403?"));
            return ResponseEntity.ok(response);
        }

        // fallback
        response.setReply("Понял. Я пока умею отвечать на базовые вопросы по счетам, переводам, кредитам и восстановлению доступа. Уточните, что именно нужно сделать?");
        response.setQuickReplies(List.of("Мои счета", "Перевод", "Кредиты", "Вклады", "Забыли пароль"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public Map<String, Object> info() {
        return Map.of(
                "name", "BankSystem Assistant",
                "version", "1.0",
                "time", LocalDateTime.now().toString(),
                "date", LocalDate.now().toString()
        );
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String msg, String... needles) {
        for (String n : needles) {
            if (msg.contains(n)) return true;
        }
        return false;
    }
}

