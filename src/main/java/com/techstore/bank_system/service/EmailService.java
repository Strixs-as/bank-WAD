package com.techstore.bank_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final Optional<JavaMailSender> mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    /**
     * Позволяет полностью выключить попытки SMTP (полезно, когда сеть/провайдер блокирует smtp.gmail.com).
     */
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    /**
     * Если true — любые ошибки SMTP логируются, но не пробрасываются наружу.
     */
    @Value("${app.mail.fail-silently:true}")
    private boolean failSilently;

    /**
     * Dev fallback: когда SMTP выключен/недоступен, пишем «письмо» в лог, чтобы UX оставался рабочим.
     */
    @Value("${app.mail.dev-log-fallback:true}")
    private boolean devLogFallback;

    /**
     * Сколько раз пробовать отправить письмо при сетевых ошибках.
     */
    @Value("${app.mail.retry.attempts:2}")
    private int retryAttempts;

    /**
     * Базовая задержка между попытками (мс), растёт линейно: base * attempt.
     */
    @Value("${app.mail.retry.base-delay-ms:700}")
    private long retryBaseDelayMs;

    public EmailService(Optional<JavaMailSender> mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String to, String subject, String text) {
        return send(to, subject, text, false);
    }

    public boolean sendHtml(String to, String subject, String html) {
        return send(to, subject, html, true);
    }

    public boolean send(String to, String subject, String body, boolean html) {
        if (to == null || to.isBlank()) {
            log.warn("EmailService: 'to' is blank. Skip sending.");
            return false;
        }

        String safeSubject = normalizeSubject(subject);

        // 1) Жёстко выключено — не трогаем сеть. В dev режиме логируем письмо.
        if (!mailEnabled) {
            if (devLogFallback) {
                logDevEmail(to, safeSubject, body, html, "app.mail.enabled=false");
                // ВАЖНО: это именно фолбэк (письмо не отправлено), поэтому возвращаем false.
                // Бизнес-логика не должна считать уведомление доставленным.
                return false;
            }

            log.info("EmailService: disabled via app.mail.enabled=false. Skip sending to={}", to);
            return false;
        }

        // 2) Включено, но JavaMailSender не сконфигурировался (нет host/port/username и т.п.)
        if (mailSender.isEmpty()) {
            if (devLogFallback) {
                logDevEmail(to, safeSubject, body, html, "JavaMailSender bean not found");
                return false;
            }

            log.error("EmailService: JavaMailSender bean not found. SMTP not configured. Skip sending to={}", to);
            return false;
        }

        int attempts = Math.max(1, retryAttempts);
        for (int i = 1; i <= attempts; i++) {
            try {
                log.info("Sending email: from={} to={} subject='{}' html={} attempt={}/{}", from, to, safeSubject, html, i, attempts);

                if (!html) {
                    SimpleMailMessage message = new SimpleMailMessage();
                    if (from != null && !from.isBlank()) {
                        message.setFrom(from);
                    }
                    message.setTo(to);
                    message.setSubject(safeSubject);
                    message.setText(body);
                    mailSender.get().send(message);
                    log.info("Email sent: to={}", to);
                    return true;
                }

                MimeMessage mimeMessage = mailSender.get().createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());

                if (from != null && !from.isBlank()) {
                    helper.setFrom(from);
                }
                helper.setTo(to);
                helper.setSubject(safeSubject);

                String plainFallback = stripHtmlToPlain(body);
                helper.setText(plainFallback, body);

                mailSender.get().send(mimeMessage);
                log.info("Email sent (HTML): to={}", to);
                return true;

            } catch (MessagingException e) {
                // Ошибки формирования сообщения (не сетевые). Повторять обычно бессмысленно.
                logFailure(to, e);
                return swallowOrThrow(e);

            } catch (MailException e) {
                boolean retryable = isRetryableMailException(e);
                logFailure(to, e);

                if (!retryable || i >= attempts) {
                    // если сеть блокирует SMTP — не превращаем это в постоянный шум: фоллбэк в лог
                    if (devLogFallback) {
                        logDevEmail(to, safeSubject, body, html, "SMTP failure: " + e.getClass().getSimpleName());
                        return false;
                    }
                    return swallowOrThrow(e);
                }

                sleepQuietly(retryBaseDelayMs * i);

            } catch (RuntimeException e) {
                logFailure(to, e);
                return swallowOrThrow(e);

            } catch (Exception e) {
                logFailure(to, e);
                return swallowOrThrow(e);
            }
        }

        return false;
    }

    private void logDevEmail(String to, String subject, String body, boolean html, String reason) {
        String preview = body == null ? "" : body;
        if (preview.length() > 800) {
            preview = preview.substring(0, 800) + "...";
        }
        log.warn("DEV EMAIL FALLBACK ({}): to={} subject='{}' html={} bodyPreview={}", reason, to, subject, html, preview);
    }

    private boolean swallowOrThrow(Exception e) {
        if (failSilently) return false;
        if (e instanceof RuntimeException re) throw re;
        throw new RuntimeException(e);
    }

    private void logFailure(String to, Exception e) {
        // Если failSilently=true, не печатаем stacktrace каждый раз (он огромный и бесит).
        if (failSilently) {
            log.warn("EmailService: failed to send email to={} :: {}: {}", to, e.getClass().getSimpleName(), e.getMessage());
        } else {
            log.error("EmailService: failed to send email to={}", to, e);
        }
    }

    private boolean isRetryableMailException(MailException e) {
        if (e instanceof MailSendException) return true;

        Throwable c = e;
        while (c != null) {
            String name = c.getClass().getName();
            if (name.contains("SocketTimeoutException")) return true;
            if (name.contains("ConnectException")) return true;
            if (name.contains("MailConnectException")) return true;
            c = c.getCause();
        }
        return false;
    }

    private void sleepQuietly(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String normalizeSubject(String subject) {
        if (subject == null) return "(no subject)";
        String s = subject.replaceAll("[^\\p{Alnum}\\s\\-_,.:()\\[\\]{}#@/+]+", " ").trim();
        s = s.replaceAll("\\s{2,}", " ");
        return s.isBlank() ? "BankSystem notification" : s;
    }

    private String stripHtmlToPlain(String html) {
        if (html == null) return "";
        String s = html
                .replaceAll("(?is)<br\\s*/?>", "\n")
                .replaceAll("(?is)</p>", "\n\n")
                .replaceAll("(?is)</div>", "\n")
                .replaceAll("(?is)<[^>]+>", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();

        s = s.replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'");

        return s;
    }
}
