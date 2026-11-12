package com.awd.logistics.service;

import com.awd.logistics.dto.WarehouseRequest;
import com.awd.logistics.dto.WarehouseResponse;
import com.awd.logistics.entity.Warehouse;
import com.awd.logistics.exception.ResourceNotFoundException;
import com.awd.logistics.mapper.LogisticsMapper;
import com.awd.logistics.repository.WarehouseRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    @Transactional(readOnly = true)
    public List<WarehouseResponse> findAll() {
        return warehouseRepository.findAll(Sort.by("id")).stream().map(LogisticsMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseResponse findById(Long id) {
        return LogisticsMapper.toResponse(getWarehouse(id));
    }

    public WarehouseResponse create(WarehouseRequest request) {
        return LogisticsMapper.toResponse(warehouseRepository.save(LogisticsMapper.toEntity(request)));
    }

    public WarehouseResponse update(Long id, WarehouseRequest request) {
        Warehouse warehouse = getWarehouse(id);
        LogisticsMapper.updateEntity(warehouse, request);
        return LogisticsMapper.toResponse(warehouseRepository.saveAndFlush(warehouse));
    }

    public void delete(Long id) {
        warehouseRepository.delete(getWarehouse(id));
    }

    private Warehouse getWarehouse(Long id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", id));
    }
}
