package com.techstore.bank_system.dto.analytics;

import java.math.BigDecimal;

public record PurchaseSummaryDto(
        BigDecimal totalAmount,
        long totalCount,
        BigDecimal avgAmount
) {
}

