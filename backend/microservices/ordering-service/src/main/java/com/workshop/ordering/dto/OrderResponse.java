package com.workshop.ordering.dto;

import com.workshop.ordering.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        Long id,
        UUID userId,
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        OrderStatus status,
        UserDTO consumer,
        List<OrderItemResponse> items
) {
}
