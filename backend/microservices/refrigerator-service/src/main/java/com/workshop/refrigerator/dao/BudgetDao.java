package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Budget;

import java.util.List;
import java.util.Optional;

public interface BudgetDao {

    List<Budget> findAll();

    Optional<Budget> findById(Long id);

    Budget save(Budget budget);

    void delete(Budget budget);

    boolean existsByYearAndMonth(Short year, Short month);

    Optional<Budget> findByYearAndMonth(Short year, Short month);
}
