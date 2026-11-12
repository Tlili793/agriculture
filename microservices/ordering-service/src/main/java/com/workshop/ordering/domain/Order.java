package com.workshop.ordering.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() {
    }

    public Order(UUID userId, LocalDateTime orderDate, BigDecimal totalAmount, OrderStatus status) {
        this.userId = userId;
        this.orderDate = orderDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return items;
    }
}
