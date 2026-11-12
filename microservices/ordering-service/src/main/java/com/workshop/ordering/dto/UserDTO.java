package com.workshop.ordering.dto;

import java.util.UUID;

public record UserDTO(UUID id, String email, String fullName) {
}
