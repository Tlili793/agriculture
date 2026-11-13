package com.workshop.ordering.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderPlacedEvent(UUID eventId, Long orderId, UUID userId, BigDecimal totalAmount) {
}
