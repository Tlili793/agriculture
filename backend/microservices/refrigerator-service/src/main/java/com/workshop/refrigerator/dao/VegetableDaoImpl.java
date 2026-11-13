package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Vegetable;
import com.workshop.refrigerator.repository.VegetableRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VegetableDaoImpl extends AbstractProduceDao<Vegetable> implements VegetableDao {

    public VegetableDaoImpl(VegetableRepository repository) {
        super(repository);
    }
}
