package com.awd.logistics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request sent by a transporter to be assigned to a delivery")
public record DeliveryAssignmentRequest(

        @Schema(description = "Keycloak subject of the requesting transporter", example = "b0cef70a-5190-400b-a4f7-590285be1065")
        @NotNull(message = "transporterId is required")
        UUID transporterId
) {
}
