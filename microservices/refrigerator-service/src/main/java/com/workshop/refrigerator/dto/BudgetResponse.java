package com.workshop.refrigerator.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record BudgetResponse(
        Long id,
        String userId,
        BigDecimal monthlyLimit,
        Short month,
        Short year,
        OffsetDateTime createdAt
) {
}
