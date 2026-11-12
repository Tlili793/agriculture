package com.workshop.refrigerator.service;

import com.workshop.refrigerator.dao.ProduceDao;
import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.exception.ResourceNotFoundException;
import com.workshop.refrigerator.model.ProduceItem;
import com.workshop.refrigerator.support.UserIds;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public abstract class AbstractProduceService<T extends ProduceItem> {

    private final ProduceDao<T> dao;

    protected AbstractProduceService(ProduceDao<T> dao) {
        this.dao = dao;
    }

    protected abstract T newInstance();

    protected abstract String resourceName();

    @Transactional(readOnly = true)
    public List<ProduceResponse> findAll() {
        return dao.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProduceResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public ProduceResponse create(ProduceRequest request) {
        validateDates(request);
        T item = newInstance();
        apply(item, request);
        return toResponse(dao.save(item));
    }

    @Transactional
    public ProduceResponse update(Long id, ProduceRequest request) {
        validateDates(request);
        T item = getEntity(id);
        apply(item, request);
        return toResponse(dao.save(item));
    }

    @Transactional
    public void delete(Long id) {
        T item = getEntity(id);
        dao.delete(item);
    }

    private T getEntity(Long id) {
        return dao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName() + " " + id + " not found"));
    }

    private void validateDates(ProduceRequest request) {
        if (request.purchaseDate() != null
                && request.expirationDate() != null
                && request.expirationDate().isBefore(request.purchaseDate())) {
            throw new IllegalArgumentException("expirationDate must not be before purchaseDate");
        }
    }

    private void apply(T item, ProduceRequest request) {
        item.setUserId(UserIds.resolve(request.userId()));
        item.setName(request.name());
        item.setQuantity(request.quantity() != null ? request.quantity() : BigDecimal.ZERO);
        item.setUnit(request.unit() != null && !request.unit().isBlank() ? request.unit() : "piece");
        item.setPrice(request.price() != null ? request.price() : BigDecimal.ZERO);
        item.setPurchaseDate(request.purchaseDate());
        item.setExpirationDate(request.expirationDate());
    }

    private ProduceResponse toResponse(T item) {
        return new ProduceResponse(
                item.getId(),
                item.getUserId(),
                item.getName(),
                item.getQuantity(),
                item.getUnit(),
                item.getPrice(),
                item.getPurchaseDate(),
                item.getExpirationDate(),
                item.getCreatedAt()
        );
    }
}
