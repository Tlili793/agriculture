package com.workshop.refrigerator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record BudgetRequest(

        @Size(max = 100, message = "userId must be at most 100 characters")
        String userId,

        @NotNull(message = "monthlyLimit is required")
        @DecimalMin(value = "0", message = "monthlyLimit must be greater than or equal to 0")
        BigDecimal monthlyLimit,

        @NotNull(message = "month is required")
        @Min(value = 1, message = "month must be between 1 and 12")
        @Max(value = 12, message = "month must be between 1 and 12")
        Short month,

        @NotNull(message = "year is required")
        @Min(value = 2000, message = "year must be greater than or equal to 2000")
        Short year
) {
}
