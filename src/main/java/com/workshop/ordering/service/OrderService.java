package com.workshop.ordering.service;

import com.workshop.ordering.domain.Order;
import com.workshop.ordering.domain.OrderItem;
import com.workshop.ordering.domain.OrderStatus;
import com.workshop.ordering.dto.CreateOrderRequest;
import com.workshop.ordering.dto.OrderResponse;
import com.workshop.ordering.event.OrderPlacedEvent;
import com.workshop.ordering.exception.OrderNotFoundException;
import com.workshop.ordering.mapper.OrderMapper;
import com.workshop.ordering.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(
            OrderRepository orderRepository,
            OrderMapper orderMapper,
            RabbitTemplate rabbitTemplate
    ) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        BigDecimal total = request.items().stream()
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order(request.userId(), LocalDateTime.now(), total, OrderStatus.PENDING);
        request.items().forEach(item -> order.addItem(new OrderItem(
                item.productId(),
                item.quantity(),
                item.unitPrice()
        )));

        Order savedOrder = orderRepository.save(order);
        rabbitTemplate.convertAndSend("platform.events", "order.placed",
                new OrderPlacedEvent(UUID.randomUUID(), savedOrder.getId(), savedOrder.getUserId(),
                        savedOrder.getTotalAmount()));
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(UUID userId) {
        return orderRepository.findAllByUserIdOrderByOrderDateDesc(userId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }
}
