package com.workshop.refrigerator.dto;

import com.workshop.refrigerator.model.MealType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record MealResponse(
        Long id,
        String userId,
        String name,
        String description,
        MealType mealType,
        LocalDate plannedDate,
        BigDecimal estimatedCost,
        String instructions,
        OffsetDateTime createdAt
) {
}
