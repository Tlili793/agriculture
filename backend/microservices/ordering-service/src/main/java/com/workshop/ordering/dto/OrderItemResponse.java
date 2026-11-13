package com.workshop.ordering.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        Integer quantity,
        BigDecimal unitPrice,
        ProductDTO product
) {
}
