package com.agri.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReclamationRequest(
        @NotNull Long consumerId,
        @NotNull Long orderId,
        @NotBlank String subject,
        @NotBlank String description
) {}
