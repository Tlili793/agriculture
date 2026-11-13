package com.workshop.refrigerator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "fruits")
public class Fruit extends ProduceItem {
}
