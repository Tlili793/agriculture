package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.ProduceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public abstract class AbstractProduceDao<T extends ProduceItem> implements ProduceDao<T> {

    private final JpaRepository<T, Long> repository;

    protected AbstractProduceDao(JpaRepository<T, Long> repository) {
        this.repository = repository;
    }

    @Override
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<T> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public T save(T item) {
        return repository.save(item);
    }

    @Override
    public void delete(T item) {
        repository.delete(item);
    }
}
