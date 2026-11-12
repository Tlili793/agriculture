package com.awd.logistics.dto;

import com.awd.logistics.entity.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Delivery returned by the API")
public record DeliveryResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "42")
        Long orderId,

        @Schema(example = "PENDING")
        DeliveryStatus status,

        @Schema(example = "2026-11-12T09:30:00", nullable = true)
        LocalDateTime dispatchedAt,

        @Schema(example = "12 Avenue Habib Bourguiba, Tunis")
        String destination
) {
}
