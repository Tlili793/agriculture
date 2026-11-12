package com.agri.reclamation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateReclamationRequest(
        @NotNull UUID userId,
        @NotNull Long orderId,
        @NotBlank String subject,
        @NotBlank String description
) {}
