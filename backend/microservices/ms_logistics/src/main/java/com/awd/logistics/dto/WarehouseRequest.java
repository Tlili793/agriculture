package com.awd.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Data used to create or update a warehouse")
public record WarehouseRequest(

        @Schema(description = "Warehouse name", example = "Tunis Central Warehouse")
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,

        @Schema(description = "Capacity for products that need a fridge", example = "500.0")
        @NotNull(message = "fridgeCapacity is required")
        @PositiveOrZero(message = "fridgeCapacity must be zero or positive")
        Double fridgeCapacity,

        @Schema(description = "Capacity for normal (non-refrigerated) products", example = "2000.0")
        @NotNull(message = "normalCapacity is required")
        @PositiveOrZero(message = "normalCapacity must be zero or positive")
        Double normalCapacity,

        @Schema(description = "Local address of the warehouse", example = "Zone Industrielle, Ben Arous")
        @NotBlank(message = "addressLocale is required")
        @Size(max = 255)
        String addressLocale
) {
}
