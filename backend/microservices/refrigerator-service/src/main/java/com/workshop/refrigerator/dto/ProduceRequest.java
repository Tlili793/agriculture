package com.workshop.refrigerator.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProduceRequest(

        @Size(max = 100, message = "userId must be at most 100 characters")
        String userId,

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @DecimalMin(value = "0", message = "quantity must be greater than or equal to 0")
        BigDecimal quantity,

        @Size(max = 20, message = "unit must be at most 20 characters")
        String unit,

        @DecimalMin(value = "0", message = "price must be greater than or equal to 0")
        BigDecimal price,

        LocalDate purchaseDate,

        LocalDate expirationDate
) {
}
