package com.techstore.bank_system.service;

import com.techstore.bank_system.entity.Account;
import com.techstore.bank_system.entity.Card;
import com.techstore.bank_system.entity.User;
import com.techstore.bank_system.repository.AccountRepository;
import com.techstore.bank_system.repository.CardRepository;
import com.techstore.bank_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired(required = false)
    private CardEmergencyTokenService cardEmergencyTokenService;

    @Autowired
    private org.springframework.core.env.Environment env;

    public Card createCard(Long userId, Long accountId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Account> accountOpt = accountRepository.findById(accountId);

        if (userOpt.isEmpty() || accountOpt.isEmpty()) {
            throw new RuntimeException("Пользователь или счёт не найден");
        }

        User user = userOpt.get();
        Account account = accountOpt.get();

        Card card = Card.builder()
                .cardNumber(generateCardNumber())
                .cardHolder(user.getFirstName() + " " + user.getLastName())
                .cvv(generateCVV())
                .expiryDate(LocalDate.now().plusYears(4))
                .isActive(true)
                .isBlocked(false)
                .createdAt(LocalDateTime.now())
                .user(user)
                .account(account)
                .build();

        Card saved = cardRepository.save(card);

        // Email-уведомление о выпуске карты
        try {
            if (emailService != null) {
                String to = user.getEmail();
                String subject = "BankSystem: карта выпущена";

                String masked = maskCardNumber(saved.getCardNumber());
                String exp = saved.getExpiryDate() != null
                        ? saved.getExpiryDate().format(DateTimeFormatter.ofPattern("MM/yy"))
                        : "—";
                String accountNumber = (saved.getAccount() != null ? saved.getAccount().getAccountNumber() : "—");

                String issuedAt = saved.getCreatedAt() != null
                        ? saved.getCreatedAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                        : LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

                String fullName = (user.getFirstName() + " " + user.getLastName()).trim();

                String ip = com.techstore.bank_system.config.RequestContext.getClientIp();
                String ua = com.techstore.bank_system.config.RequestContext.getUserAgent();

                String emergencyUrl = "";
                if (cardEmergencyTokenService != null) {
                    String token = cardEmergencyTokenService.issueToken(saved.getId());
                    String port = env != null ? env.getProperty("server.port", "8080") : "8080";
                    emergencyUrl = "http://localhost:" + port + "/card-block-confirm?token=" + token;
                }

                String blockButtonHtml = "";
                if (emergencyUrl != null && !emergencyUrl.isBlank()) {
                    // ВАЖНО: никаких String::format / String::formatted — иначе любой '%' из URL/UA может ломать Formatter
                    blockButtonHtml = "<div style='margin-top:14px;text-align:center;'>"
                            + "<a href='" + escapeHtml(emergencyUrl) + "' style='display:inline-block;padding:12px 16px;border-radius:14px;"
                            + "background:linear-gradient(135deg,#ff4d4d,#ff1f6d);color:#fff;text-decoration:none;font-weight:900;'>"
                            + "Экстренно заблокировать карту"
                            + "</a>"
                            + "<div style='margin-top:8px;font-size:12px;opacity:.70;'>Ссылка действует ограниченное время (для вашей безопасности).</div>"
                            + "</div>";
                }

                // HTML
                StringBuilder hb = new StringBuilder();
                hb.append("<!doctype html>")
                        .append("<html lang='ru'>")
                        .append("<head>")
                        .append("<meta charset='utf-8'/>")
                        .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'/>")
                        .append("<title>Карта выпущена — BankSystem</title>")
                        .append("</head>")
                        .append("<body style='margin:0;padding:0;background:#0b1220;font-family:Segoe UI,Arial,sans-serif;color:#e6e9f2;'>")
                        .append("<div style='max-width:680px;margin:0 auto;padding:26px 14px;'>")

                        .append("<div style='background:linear-gradient(135deg,#667eea,#764ba2);border-radius:18px;padding:18px 18px 16px;'>")
                        .append("<div style='display:flex;justify-content:space-between;align-items:flex-start;gap:12px;'>")
                        .append("<div>")
                        .append("<div style='font-size:20px;font-weight:800;'>BankSystem</div>")
                        .append("<div style='opacity:.9;font-size:13px;margin-top:4px;'>Уведомление о выпуске карты</div>")
                        .append("</div>")
                        .append("<div style='font-size:12px;opacity:.85;text-align:right;'>")
                        .append(escapeHtml(issuedAt))
                        .append("</div>")
                        .append("</div>")
                        .append("</div>")

                        .append("<div style='background:#0f1a33;border:1px solid rgba(255,255,255,.08);border-radius:18px;padding:18px;margin-top:14px;'>")
                        .append("<div style='font-size:16px;font-weight:800;margin-bottom:8px;'>Здравствуйте, ")
                        .append(escapeHtml(fullName.isBlank() ? "клиент" : fullName))
                        .append("!</div>")
                        .append("<div style='opacity:.92;line-height:1.6;'>Мы выпустили новую карту для вашего аккаунта. Ниже — безопасные детали.</div>")

                        .append("<div style='margin-top:14px;border-radius:18px;padding:16px 16px 14px;background:radial-gradient(120% 120% at 10% 0%, rgba(240,147,251,0.45) 0%, rgba(102,126,234,0.16) 42%, rgba(118,75,162,0.10) 100%), linear-gradient(135deg,#111a33,#0f1a33);border:1px solid rgba(255,255,255,.10);'>")
                        .append("<div style='display:flex;justify-content:space-between;align-items:center;'>")
                        .append("<div style='font-size:13px;opacity:.9;font-weight:700;'>BankSystem • Card</div>")
                        .append("<div style='font-size:12px;opacity:.85;'>Virtual / Secure</div>")
                        .append("</div>")
                        .append("<div style='margin-top:14px;font-size:18px;letter-spacing:2px;font-weight:800;'>")
                        .append(escapeHtml(masked))
                        .append("</div>")
                        .append("<div style='display:flex;gap:14px;justify-content:space-between;align-items:flex-end;margin-top:14px;'>")
                        .append("<div><div style='font-size:11px;opacity:.7;'>CARD HOLDER</div><div style='font-size:13px;font-weight:800;'>")
                        .append(escapeHtml(saved.getCardHolder()))
                        .append("</div></div>")
                        .append("<div style='text-align:right;'><div style='font-size:11px;opacity:.7;'>EXP</div><div style='font-size:13px;font-weight:800;'>")
                        .append(escapeHtml(exp))
                        .append("</div></div>")
                        .append("</div>")
                        .append("</div>")

                        .append("<div style='margin-top:14px;padding:14px;border-radius:14px;background:rgba(255,255,255,.04);border:1px solid rgba(255,255,255,.08);'>")
                        .append("<div style='font-size:13px;opacity:.8;margin-bottom:8px;'>Где выполнено действие</div>")
                        .append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>")
                        .append("<tr><td style='padding:7px 0;opacity:.75;'>IP адрес</td><td style='padding:7px 0;font-weight:800;text-align:right;'>")
                        .append(escapeHtml(ip != null ? ip : "—"))
                        .append("</td></tr>")
                        .append("<tr><td style='padding:7px 0;opacity:.75;'>Устройство / браузер</td><td style='padding:7px 0;font-weight:700;text-align:right;'>")
                        .append(escapeHtml(ua != null ? shorten(ua, 120) : "—"))
                        .append("</td></tr>")
                        .append("</table>")
                        .append("</div>")

                        .append("<div style='margin-top:14px;padding:14px;border-radius:14px;background:rgba(255,255,255,.04);border:1px solid rgba(255,255,255,.08);'>")
                        .append("<div style='font-size:13px;opacity:.8;margin-bottom:8px;'>Детали выпуска</div>")
                        .append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>")
                        .append("<tr><td style='padding:7px 0;opacity:.75;'>Привязанный счёт</td><td style='padding:7px 0;font-weight:800;text-align:right;'>")
                        .append(escapeHtml(accountNumber))
                        .append("</td></tr>")
                        .append("<tr><td style='padding:7px 0;opacity:.75;'>Статус</td><td style='padding:7px 0;font-weight:800;text-align:right;'>Активна</td></tr>")
                        .append("</table>")
                        .append("</div>")

                        .append(blockButtonHtml)

                        .append("<div style='margin-top:14px;font-size:12px;opacity:.75;line-height:1.5;'>Это автоматическое уведомление. Мы никогда не просим пароли в письмах.</div>")
                        .append("</div>")

                        .append("<div style='text-align:center;margin-top:14px;font-size:12px;opacity:.55;'>© BankSystem • Makesh Naiman ВТиПО-33 • makeshnaiman@gmail.com</div>")
                        .append("</div>")
                        .append("</body>")
                        .append("</html>");

                String html = hb.toString();

                boolean ok = emailService.sendHtml(to, subject, html);
                if (!ok) {
                    // Fallback на текст (без String::format)
                    StringBuilder sb = new StringBuilder();
                    sb.append("BankSystem — карта выпущена\n\n")
                            .append("Здравствуйте, ").append(fullName.isBlank() ? "клиент" : fullName).append("!\n")
                            .append("Мы выпустили новую карту для вашего аккаунта.\n\n")
                            .append("Карта: ").append(masked).append("\n")
                            .append("Действует до: ").append(exp).append("\n")
                            .append("Привязанный счёт: ").append(accountNumber).append("\n\n")
                            .append("Где выполнено действие\n")
                            .append("IP: ").append(ip != null ? ip : "—").append("\n")
                            .append("User-Agent: ").append(ua != null ? shorten(ua, 140) : "—").append("\n\n")
                            .append("Если это не вы — срочно заблокируйте карту и смените пароль.\n");

                    if (emergencyUrl != null && !emergencyUrl.isBlank()) {
                        sb.append("Экстренная блокировка: ").append(emergencyUrl).append("\n");
                    }

                    sb.append("\n© BankSystem\n");

                    emailService.sendEmail(to, subject, sb.toString());
                }
            }
        } catch (Exception ex) {
            System.err.println("❌ CardService: failed to send card creation email: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            ex.printStackTrace(System.err);
        }

        return saved;
    }

    private String shorten(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    @Transactional
    public Card blockCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsBlocked(true);
        card.setBlockedAt(LocalDateTime.now());
        return cardRepository.save(card);
    }

    @Transactional
    public Card unblockCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsBlocked(false);
        card.setBlockedAt(null);
        return cardRepository.save(card);
    }

    @Transactional
    public Card deactivateCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isEmpty()) {
            throw new RuntimeException("Карта не найдена");
        }

        Card card = cardOpt.get();
        card.setIsActive(false);
        return cardRepository.save(card);
    }

    public List<Card> getUserCards(Long userId) {
        return cardRepository.findByUserId(userId);
    }

    public List<Card> getActiveUserCards(Long userId) {
        return cardRepository.findActiveCards(userId);
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    /**
     * Админская операция: заблокировать и деактивировать карту (soft delete).
     * Физически запись не удаляем — иначе можем сломать FK на account/user.
     */
    @Transactional
    public Card adminBlockAndDeactivate(Long cardId, String reason) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));

        if (!Boolean.TRUE.equals(card.getIsBlocked())) {
            card.setIsBlocked(true);
            card.setBlockedAt(LocalDateTime.now());
        }
        if (Boolean.TRUE.equals(card.getIsActive())) {
            card.setIsActive(false);
        }

        Card saved = cardRepository.save(card);

        // уведомление пользователю
        try {
            if (emailService != null && saved.getUser() != null) {
                sendCardBlockedAndDeletedEmail(saved, reason);
            }
        } catch (Exception ex) {
            System.err.println("❌ CardService: failed to send card delete email: " + ex.getMessage());
        }

        return saved;
    }

    private void sendCardBlockedAndDeletedEmail(Card card, String reason) {
        String to = card.getUser() != null ? card.getUser().getEmail() : null;
        if (to == null || to.isBlank()) return;

        String masked = maskCardNumber(card.getCardNumber());
        String fullName = "";
        if (card.getUser() != null) {
            fullName = (String.valueOf(card.getUser().getFirstName()) + " " + String.valueOf(card.getUser().getLastName())).trim();
        }

        String when = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        String ip = com.techstore.bank_system.config.RequestContext.getClientIp();
        String ua = com.techstore.bank_system.config.RequestContext.getUserAgent();

        String subject = "BankSystem: карта заблокирована и удалена";

        String safeReason = (reason == null || reason.isBlank()) ? "Административное действие" : reason;

        StringBuilder sb = new StringBuilder();
        sb.append("Здравствуйте, ").append(fullName.isBlank() ? "клиент" : fullName).append("!\n\n")
                .append("В BankSystem ваша карта была заблокирована и удалена из системы.\n\n")
                .append("Карта: ").append(masked).append("\n")
                .append("Время: ").append(when).append("\n")
                .append("Причина: ").append(safeReason).append("\n")
                .append("IP: ").append(ip != null ? ip : "—").append("\n")
                .append("Устройство: ").append(ua != null ? shorten(ua, 140) : "—").append("\n\n")
                .append("Если это были не вы — срочно смените пароль и обратитесь в поддержку.\n")
                .append("\n© BankSystem\n");

        emailService.sendEmail(to, subject, sb.toString());
    }

    public boolean isCardValid(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    private String generateCardNumber() {
        // Простой генератор 16 цифр (демо)
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String generateCVV() {
        java.util.Random random = new java.util.Random();
        int cvv = 100 + random.nextInt(900);
        return String.valueOf(cvv);
    }

    // --- helpers ---
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null) return "**** **** **** ****";
        String digits = cardNumber.replaceAll("\\D+", "");
        if (digits.length() < 8) return "****";
        String last4 = digits.substring(Math.max(0, digits.length() - 4));
        return "**** **** **** " + last4;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
