package com.workshop.refrigerator.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vegetables")
public class Vegetable extends ProduceItem {
}
