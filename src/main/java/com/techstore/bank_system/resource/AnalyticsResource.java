package com.techstore.bank_system.resource;

import com.techstore.bank_system.dto.analytics.BreakdownItemDto;
import com.techstore.bank_system.dto.analytics.PurchaseSummaryDto;
import com.techstore.bank_system.dto.analytics.TimeSeriesPointDto;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.service.AnalyticsService;
import com.techstore.bank_system.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsResource {

    private final AnalyticsService analyticsService;
    private final JwtUtil jwtUtil;

    public AnalyticsResource(AnalyticsService analyticsService, JwtUtil jwtUtil) {
        this.analyticsService = analyticsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/purchases/summary")
    public ResponseEntity<PurchaseSummaryDto> summary(@RequestHeader("Authorization") String authHeader,
                                                      @RequestParam(required = false) String accountNumber,
                                                      @RequestParam(required = false) CurrencyType currency,
                                                      @RequestParam(required = false) LocalDate from,
                                                      @RequestParam(required = false) LocalDate to) {
        Long userId = jwtUtil.extractUserId(extractToken(authHeader));
        return ResponseEntity.ok(analyticsService.purchaseSummary(userId, accountNumber, currency, from, to));
    }

    @GetMapping("/purchases/by-day")
    public ResponseEntity<List<TimeSeriesPointDto>> byDay(@RequestHeader("Authorization") String authHeader,
                                                          @RequestParam(required = false) String accountNumber,
                                                          @RequestParam(required = false) CurrencyType currency,
                                                          @RequestParam(required = false) LocalDate from,
                                                          @RequestParam(required = false) LocalDate to) {
        Long userId = jwtUtil.extractUserId(extractToken(authHeader));
        return ResponseEntity.ok(analyticsService.purchasesByDay(userId, accountNumber, currency, from, to));
    }

    @GetMapping("/purchases/by-category")
    public ResponseEntity<List<BreakdownItemDto>> byCategory(@RequestHeader("Authorization") String authHeader,
                                                             @RequestParam(required = false) String accountNumber,
                                                             @RequestParam(required = false) CurrencyType currency,
                                                             @RequestParam(required = false) LocalDate from,
                                                             @RequestParam(required = false) LocalDate to,
                                                             @RequestParam(defaultValue = "10") int limit) {
        Long userId = jwtUtil.extractUserId(extractToken(authHeader));
        return ResponseEntity.ok(analyticsService.purchasesByCategory(userId, accountNumber, currency, from, to, limit));
    }

    @GetMapping("/purchases/by-merchant")
    public ResponseEntity<List<BreakdownItemDto>> byMerchant(@RequestHeader("Authorization") String authHeader,
                                                             @RequestParam(required = false) String accountNumber,
                                                             @RequestParam(required = false) CurrencyType currency,
                                                             @RequestParam(required = false) LocalDate from,
                                                             @RequestParam(required = false) LocalDate to,
                                                             @RequestParam(defaultValue = "10") int limit) {
        Long userId = jwtUtil.extractUserId(extractToken(authHeader));
        return ResponseEntity.ok(analyticsService.purchasesByMerchant(userId, accountNumber, currency, from, to, limit));
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Missing or invalid authorization header");
    }
}

