package com.workshop.ordering.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateOrderItemRequest(
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity,
        @NotNull @PositiveOrZero BigDecimal unitPrice
) {
}
