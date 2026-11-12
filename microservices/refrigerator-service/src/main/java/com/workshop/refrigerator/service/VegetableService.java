package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.VegetableDao;
import com.workshop.refrigerator.model.Vegetable;
import org.springframework.stereotype.Service;

@Service
public class VegetableService extends AbstractProduceService<Vegetable> {

    public VegetableService(VegetableDao dao) {
        super(dao);
    }

    @Override
    protected Vegetable newInstance() {
        return new Vegetable();
    }

    @Override
    protected String resourceName() {
        return "Vegetable";
    }
}
