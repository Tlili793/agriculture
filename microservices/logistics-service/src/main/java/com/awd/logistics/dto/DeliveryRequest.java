package com.awd.logistics.dto;

import com.awd.logistics.entity.DeliveryStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Data used to create or update a delivery")
public record DeliveryRequest(

        @Schema(description = "Id of the order to deliver (from the Ordering microservice)", example = "42")
        @NotNull(message = "orderId is required")
        Long orderId,

        @Schema(description = "Delivery status. Defaults to PENDING on creation; kept unchanged on update if omitted.",
                example = "PENDING")
        DeliveryStatus status,

        @Schema(description = "Date and time the delivery left the warehouse", example = "2026-11-12T09:30:00",
                nullable = true)
        LocalDateTime dispatchedAt,

        @Schema(description = "Delivery destination address", example = "12 Avenue Habib Bourguiba, Tunis")
        @NotBlank(message = "destination is required")
        @Size(max = 255)
        String destination
) {
}
