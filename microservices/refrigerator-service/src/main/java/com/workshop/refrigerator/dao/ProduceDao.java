package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.ProduceItem;

import java.util.List;
import java.util.Optional;

public interface ProduceDao<T extends ProduceItem> {

    List<T> findAll();

    Optional<T> findById(Long id);

    T save(T item);

    void delete(T item);
}
