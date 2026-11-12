package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.MealDao;
import com.workshop.refrigerator.dto.MealRequest;
import com.workshop.refrigerator.dto.MealResponse;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Meal;
import com.workshop.refrigerator.support.UserIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class MealService {

    private final MealDao dao;

    public MealService(MealDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    public List<MealResponse> findAll() {
        return dao.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MealResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public MealResponse create(MealRequest request) {
        Meal meal = new Meal();
        apply(meal, request);
        return toResponse(dao.save(meal));
    }

    public MealResponse update(Long id, MealRequest request) {
        Meal meal = getEntity(id);
        apply(meal, request);
        return toResponse(dao.save(meal));
    }

    public void delete(Long id) {
        Meal meal = getEntity(id);
        dao.delete(meal);
    }

    private Meal getEntity(Long id) {
        return dao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meal " + id + " not found"));
    }

    private void apply(Meal meal, MealRequest request) {
        meal.setUserId(UserIds.resolve(request.userId()));
        meal.setName(request.name());
        meal.setDescription(request.description());
        meal.setMealType(request.mealType());
        meal.setPlannedDate(request.plannedDate());
        meal.setEstimatedCost(request.estimatedCost() != null ? request.estimatedCost() : BigDecimal.ZERO);
        meal.setInstructions(request.instructions());
    }

    private MealResponse toResponse(Meal meal) {
        return new MealResponse(
                meal.getId(),
                meal.getUserId(),
                meal.getName(),
                meal.getDescription(),
                meal.getMealType(),
                meal.getPlannedDate(),
                meal.getEstimatedCost(),
                meal.getInstructions(),
                meal.getCreatedAt()
        );
    }
}
