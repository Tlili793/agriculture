package com.workshop.refrigerator.repository;

import com.workshop.refrigerator.model.Vegetable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VegetableRepository extends JpaRepository<Vegetable, Long> {
}
