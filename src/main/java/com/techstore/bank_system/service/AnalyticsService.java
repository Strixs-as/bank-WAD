package com.techstore.bank_system.service;

import com.techstore.bank_system.dto.analytics.BreakdownItemDto;
import com.techstore.bank_system.dto.analytics.PurchaseSummaryDto;
import com.techstore.bank_system.dto.analytics.TimeSeriesPointDto;
import com.techstore.bank_system.entity.CurrencyType;
import com.techstore.bank_system.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public PurchaseSummaryDto purchaseSummary(Long userId, String accountNumber, CurrencyType currency,
                                              LocalDate from, LocalDate to) {
        DateRange range = DateRange.of(from, to);
        return analyticsRepository.purchaseSummary(userId, accountNumber, currency, range.from, range.to);
    }

    public List<TimeSeriesPointDto> purchasesByDay(Long userId, String accountNumber, CurrencyType currency,
                                                   LocalDate from, LocalDate to) {
        DateRange range = DateRange.of(from, to);
        return analyticsRepository.purchasesByDay(userId, accountNumber, currency, range.from, range.to);
    }

    public List<BreakdownItemDto> purchasesByCategory(Long userId, String accountNumber, CurrencyType currency,
                                                      LocalDate from, LocalDate to, int limit) {
        DateRange range = DateRange.of(from, to);
        return analyticsRepository.purchasesByCategory(userId, accountNumber, currency, range.from, range.to, limit);
    }

    public List<BreakdownItemDto> purchasesByMerchant(Long userId, String accountNumber, CurrencyType currency,
                                                      LocalDate from, LocalDate to, int limit) {
        DateRange range = DateRange.of(from, to);
        return analyticsRepository.purchasesByMerchant(userId, accountNumber, currency, range.from, range.to, limit);
    }

    private record DateRange(LocalDateTime from, LocalDateTime to) {
        static DateRange of(LocalDate from, LocalDate to) {
            LocalDate safeTo = (to == null) ? LocalDate.now() : to;
            LocalDate safeFrom = (from == null) ? safeTo.minusDays(30) : from;
            if (safeFrom.isAfter(safeTo)) {
                // swap
                LocalDate tmp = safeFrom;
                safeFrom = safeTo;
                safeTo = tmp;
            }
            // begin/end of day
            return new DateRange(safeFrom.atStartOfDay(), safeTo.atTime(LocalTime.MAX));
        }
    }
}

