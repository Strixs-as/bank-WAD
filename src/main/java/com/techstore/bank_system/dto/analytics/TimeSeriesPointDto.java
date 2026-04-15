package com.techstore.bank_system.dto.analytics;

import java.math.BigDecimal;

public record TimeSeriesPointDto(
        String date,
        BigDecimal totalAmount,
        long totalCount
) {
}

