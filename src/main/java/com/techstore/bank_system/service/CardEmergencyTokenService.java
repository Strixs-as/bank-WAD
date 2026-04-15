package com.techstore.bank_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

/**
 * Одноразовая подпись для ссылки из email (экстренная блокировка карты).
 *
 * Токен не хранится в БД: он вычисляемый и проверяемый.
 * Формат: base64url(cardId.exp.HEX_HMAC)
 *
 * exp — epochSeconds, по умолчанию 15 минут.
 */
@Service
public class CardEmergencyTokenService {

    @Value("${bank.security.card-email-secret:BankSystemCardEmailSecretChangeMe}")
    private String secret;

    @Value("${bank.security.card-email-ttl-seconds:900}")
    private long ttlSeconds;

    public String issueToken(long cardId) {
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = cardId + "." + exp;
        String sig = hmacHex(payload);
        String raw = payload + "." + sig;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * @return cardId если токен валиден и не истёк, иначе null
     */
    public Long validateAndExtractCardId(String token) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\.");
            if (parts.length != 3) return null;
            long cardId = Long.parseLong(parts[0]);
            long exp = Long.parseLong(parts[1]);
            String sig = parts[2];

            if (Instant.now().getEpochSecond() > exp) return null;
            String expected = hmacHex(parts[0] + "." + parts[1]);
            if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            return cardId;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String hmacHex(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create HMAC", e);
        }
    }
}

