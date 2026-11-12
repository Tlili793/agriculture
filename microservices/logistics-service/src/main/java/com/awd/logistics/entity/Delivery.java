package com.awd.logistics.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Id of the order (owned by the Ordering microservice). Plain value: no foreign key, no @ManyToOne. */
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(nullable = false)
    private String destination;

    public Delivery() {
    }

    public Delivery(Long orderId, DeliveryStatus status, LocalDateTime dispatchedAt, String destination) {
        this.orderId = orderId;
        this.status = status;
        this.dispatchedAt = dispatchedAt;
        this.destination = destination;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}
