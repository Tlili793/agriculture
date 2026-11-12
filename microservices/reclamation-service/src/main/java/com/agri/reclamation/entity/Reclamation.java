package com.agri.reclamation.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Reclamation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private UUID userId;       // who complains (Keycloak user id)
    private Long orderId;      // which order (lives in MS2 Ordering, so only the id)

    private String subject;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    private ReclamationStatus status;

    @Column(length = 1000)
    private String adminResponse;  // the admin's answer

    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        status = ReclamationStatus.OPEN;
    }

    public Long getId() { return id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ReclamationStatus getStatus() { return status; }
    public void setStatus(ReclamationStatus status) { this.status = status; }
    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
