package com.workshop.refrigerator.repository;

import com.workshop.refrigerator.model.Meal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, Long> {
}
