package com.awd.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Warehouse returned by the API")
public record WarehouseResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Tunis Central Warehouse")
        String name,

        @Schema(example = "500.0")
        Double fridgeCapacity,

        @Schema(example = "2000.0")
        Double normalCapacity,

        @Schema(example = "Zone Industrielle, Ben Arous")
        String addressLocale
) {
}
