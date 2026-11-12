package com.workshop.refrigerator.dao;

import com.workshop.refrigerator.model.Fruit;
import com.workshop.refrigerator.repository.FruitRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FruitDaoImpl extends AbstractProduceDao<Fruit> implements FruitDao {

    public FruitDaoImpl(FruitRepository repository) {
        super(repository);
    }
}
