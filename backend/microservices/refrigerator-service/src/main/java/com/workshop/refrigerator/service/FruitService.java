package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.FruitDao;
import com.workshop.refrigerator.model.Fruit;
import org.springframework.stereotype.Service;

@Service
public class FruitService extends AbstractProduceService<Fruit> {

    public FruitService(FruitDao dao) {
        super(dao);
    }

    @Override
    protected Fruit newInstance() {
        return new Fruit();
    }

    @Override
    protected String resourceName() {
        return "Fruit";
    }
}
