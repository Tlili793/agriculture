package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Meal;
import com.workshop.refrigerator.repository.MealRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MealDaoImpl implements MealDao {

    private final MealRepository repository;

    public MealDaoImpl(MealRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Meal> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Meal> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public Meal save(Meal meal) {
        return repository.save(meal);
    }

    @Override
    public void delete(Meal meal) {
        repository.delete(meal);
    }
}
