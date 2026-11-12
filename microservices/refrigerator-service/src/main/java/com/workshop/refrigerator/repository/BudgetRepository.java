package com.workshop.refrigerator.repository;

import com.workshop.refrigerator.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByYearAndMonth(Short year, Short month);

    boolean existsByYearAndMonth(Short year, Short month);
}
