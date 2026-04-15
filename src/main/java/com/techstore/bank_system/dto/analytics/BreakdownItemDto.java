package com.techstore.bank_system.dto.analytics;

import java.math.BigDecimal;

public record BreakdownItemDto(
        String label,
        BigDecimal totalAmount,
        long totalCount
) {
}

