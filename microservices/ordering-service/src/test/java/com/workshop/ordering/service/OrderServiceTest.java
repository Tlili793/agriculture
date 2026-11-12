package com.workshop.ordering.service;

import com.workshop.ordering.domain.Order;
import com.workshop.ordering.dto.CreateOrderItemRequest;
import com.workshop.ordering.dto.CreateOrderRequest;
import com.workshop.ordering.event.OrderPlacedEvent;
import com.workshop.ordering.exception.OrderNotFoundException;
import com.workshop.ordering.mapper.OrderMapper;
import com.workshop.ordering.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                new OrderMapper(),
                rabbitTemplate
        );
    }

    @Test
    void createOrderCalculatesTotalPersistsItemsAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", 42L);
            return order;
        });

        var response = orderService.createOrder(new CreateOrderRequest(
                userId,
                List.of(
                        new CreateOrderItemRequest(11L, 2, new BigDecimal("12.50")),
                        new CreateOrderItemRequest(11L, 3, new BigDecimal("12.50"))
                )
        ));

        assertEquals(42L, response.id());
        assertEquals(userId, response.userId());
        assertEquals(new BigDecimal("62.50"), response.totalAmount());
        assertEquals(2, response.items().size());
        assertEquals(new BigDecimal("12.50"), response.items().get(0).unitPrice());
        assertEquals(11L, response.items().get(0).product().id());
        assertEquals("Product 11", response.items().get(0).product().name());
        assertEquals(new BigDecimal("12.50"), response.items().get(0).product().price());
        assertEquals(userId, response.consumer().id());
        assertNull(response.consumer().email());
        assertNull(response.consumer().fullName());

        ArgumentCaptor<OrderPlacedEvent> eventCaptor = ArgumentCaptor.forClass(OrderPlacedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq("platform.events"),
                org.mockito.ArgumentMatchers.eq("order.placed"),
                eventCaptor.capture()
        );
        assertNotNull(eventCaptor.getValue().eventId());
        assertEquals(42L, eventCaptor.getValue().orderId());
        assertEquals(userId, eventCaptor.getValue().userId());
        assertEquals(new BigDecimal("62.50"), eventCaptor.getValue().totalAmount());
    }

    @Test
    void getOrderThrowsWhenOrderDoesNotExist() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrder(404L));
        verifyNoInteractions(rabbitTemplate);
    }
}
