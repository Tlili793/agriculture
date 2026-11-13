package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Budget;
import com.workshop.refrigerator.repository.BudgetRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class BudgetDaoImpl implements BudgetDao {

    private final BudgetRepository repository;

    public BudgetDaoImpl(BudgetRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Budget> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Budget> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Budget save(Budget budget) {
        return repository.save(budget);
    }

    @Override
    public void delete(Budget budget) {
        repository.delete(budget);
    }

    @Override
    public boolean existsByYearAndMonth(Short year, Short month) {
        return repository.existsByYearAndMonth(year, month);
    }

    @Override
    public Optional<Budget> findByYearAndMonth(Short year, Short month) {
        return repository.findByYearAndMonth(year, month);
    }
}
