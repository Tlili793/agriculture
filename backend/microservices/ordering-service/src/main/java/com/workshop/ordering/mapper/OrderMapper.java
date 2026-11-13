package com.workshop.ordering.mapper;

import com.workshop.ordering.domain.Order;
import com.workshop.ordering.domain.OrderItem;
import com.workshop.ordering.dto.OrderItemResponse;
import com.workshop.ordering.dto.OrderResponse;
import com.workshop.ordering.dto.ProductDTO;
import com.workshop.ordering.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        var items = order.getItems().stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                new UserDTO(order.getUserId(), null, null),
                items
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        ProductDTO product = new ProductDTO(
                item.getProductId(),
                "Product " + item.getProductId(),
                item.getUnitPrice()
        );
        return new OrderItemResponse(
                item.getId(),
                item.getProductId(),
                item.getQuantity(),
                item.getUnitPrice(),
                product
        );
    }
}
