package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.BudgetDao;
import com.workshop.refrigerator.dto.BudgetRequest;
import com.workshop.refrigerator.dto.BudgetResponse;
import com.workshop.refrigerator.exception.ConflictException;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.Budget;
import com.workshop.refrigerator.support.UserIds;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BudgetService {

    private final BudgetDao dao;

    public BudgetService(BudgetDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> findAll() {
        return dao.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BudgetResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    public BudgetResponse create(BudgetRequest request) {
        if (dao.existsByYearAndMonth(request.year(), request.month())) {
            throw new ConflictException("A budget already exists for " + request.month() + "/" + request.year());
        }
        Budget budget = new Budget();
        apply(budget, request);
        return toResponse(dao.save(budget));
    }

    public BudgetResponse update(Long id, BudgetRequest request) {
        Budget budget = getEntity(id);
        dao.findByYearAndMonth(request.year(), request.month())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "A budget already exists for " + request.month() + "/" + request.year());
                });
        apply(budget, request);
        return toResponse(dao.save(budget));
    }

    public void delete(Long id) {
        Budget budget = getEntity(id);
        dao.delete(budget);
    }

    private Budget getEntity(Long id) {
        return dao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget " + id + " not found"));
    }

    private void apply(Budget budget, BudgetRequest request) {
        budget.setUserId(UserIds.resolve(request.userId()));
        budget.setMonthlyLimit(request.monthlyLimit());
        budget.setMonth(request.month());
        budget.setYear(request.year());
    }

    private BudgetResponse toResponse(Budget budget) {
        return new BudgetResponse(
                budget.getId(),
                budget.getUserId(),
                budget.getMonthlyLimit(),
                budget.getMonth(),
                budget.getYear(),
                budget.getCreatedAt()
        );
    }
}
