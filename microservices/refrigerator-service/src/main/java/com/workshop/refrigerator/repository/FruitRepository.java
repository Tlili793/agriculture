package com.workshop.refrigerator.repository;

import com.workshop.refrigerator.model.Fruit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FruitRepository extends JpaRepository<Fruit, Long> {
}
