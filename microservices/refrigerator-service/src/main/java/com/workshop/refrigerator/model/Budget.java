package com.workshop.refrigerator.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "budget",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_budget_year_month",
                columnNames = {"year", "month"}
        )
)
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "monthly_limit", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(name = "month", nullable = false)
    private Short month;

    @Column(name = "year", nullable = false)
    private Short year;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public Short getMonth() {
        return month;
    }

    public void setMonth(Short month) {
        this.month = month;
    }

    public Short getYear() {
        return year;
    }

    public void setYear(Short year) {
        this.year = year;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
