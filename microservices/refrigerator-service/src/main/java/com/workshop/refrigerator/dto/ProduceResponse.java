package com.workshop.refrigerator.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record ProduceResponse(
        Long id,
        String userId,
        String name,
        BigDecimal quantity,
        String unit,
        BigDecimal price,
        LocalDate purchaseDate,
        LocalDate expirationDate,
        OffsetDateTime createdAt
) {
}
