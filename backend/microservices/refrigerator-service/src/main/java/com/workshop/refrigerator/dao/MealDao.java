package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Meal;

import java.util.List;
import java.util.Optional;

public interface MealDao {

    List<Meal> findAll();

    Optional<Meal> findById(Long id);

    Meal save(Meal meal);

    void delete(Meal meal);
}
