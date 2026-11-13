package com.awd.logistics.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse")
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "fridge_capacity", nullable = false)
    private Double fridgeCapacity;

    @Column(name = "normal_capacity", nullable = false)
    private Double normalCapacity;

    @Column(name = "address_locale", nullable = false)
    private String addressLocale;

    public Warehouse() {
    }

    public Warehouse(String name, Double fridgeCapacity, Double normalCapacity, String addressLocale) {
        this.name = name;
        this.fridgeCapacity = fridgeCapacity;
        this.normalCapacity = normalCapacity;
        this.addressLocale = addressLocale;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getFridgeCapacity() { return fridgeCapacity; }
    public void setFridgeCapacity(Double fridgeCapacity) { this.fridgeCapacity = fridgeCapacity; }

    public Double getNormalCapacity() { return normalCapacity; }
    public void setNormalCapacity(Double normalCapacity) { this.normalCapacity = normalCapacity; }

    public String getAddressLocale() { return addressLocale; }
    public void setAddressLocale(String addressLocale) { this.addressLocale = addressLocale; }
}
