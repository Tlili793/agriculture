package com.agri.reclamation.dto;

import com.agri.reclamation.entity.ReclamationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull ReclamationStatus status,
        String adminResponse
) {}
