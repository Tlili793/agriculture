package com.workshop.refrigerator.dto;

import com.workshop.refrigerator.model.MealType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MealRequest(

        @Size(max = 100, message = "userId must be at most 100 characters")
        String userId,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        String description,

        @NotNull(message = "mealType is required")
        MealType mealType,

        LocalDate plannedDate,

        @DecimalMin(value = "0", message = "estimatedCost must be greater than or equal to 0")
        BigDecimal estimatedCost,

        String instructions
) {
}
