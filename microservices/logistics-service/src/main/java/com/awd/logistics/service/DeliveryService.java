package com.awd.logistics.service;

import com.awd.logistics.dto.DeliveryRequest;
import com.awd.logistics.dto.DeliveryResponse;
import com.awd.logistics.entity.Delivery;
import com.awd.logistics.entity.DeliveryStatus;
import com.awd.logistics.exception.ResourceNotFoundException;
import com.awd.logistics.mapper.LogisticsMapper;
import com.awd.logistics.repository.DeliveryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryService(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> findAll(DeliveryStatus status, Long orderId) {
        List<Delivery> deliveries;
        if (status != null && orderId != null) {
            deliveries = deliveryRepository.findByStatusAndOrderIdOrderByIdAsc(status, orderId);
        } else if (status != null) {
            deliveries = deliveryRepository.findByStatusOrderByIdAsc(status);
        } else if (orderId != null) {
            deliveries = deliveryRepository.findByOrderIdOrderByIdAsc(orderId);
        } else {
            deliveries = deliveryRepository.findAll(Sort.by("id"));
        }
        return deliveries.stream().map(LogisticsMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DeliveryResponse findById(Long id) {
        return LogisticsMapper.toResponse(getDelivery(id));
    }

    public DeliveryResponse create(DeliveryRequest request) {
        return LogisticsMapper.toResponse(deliveryRepository.save(LogisticsMapper.toEntity(request)));
    }

    public DeliveryResponse update(Long id, DeliveryRequest request) {
        Delivery delivery = getDelivery(id);
        LogisticsMapper.updateEntity(delivery, request);
        return LogisticsMapper.toResponse(deliveryRepository.saveAndFlush(delivery));
    }

    /** Changes the status only. Moving to DISPATCHED stamps dispatchedAt if it is still empty. */
    public DeliveryResponse updateStatus(Long id, DeliveryStatus status) {
        Delivery delivery = getDelivery(id);
        delivery.setStatus(status);
        if (status == DeliveryStatus.DISPATCHED && delivery.getDispatchedAt() == null) {
            delivery.setDispatchedAt(LocalDateTime.now());
        }
        return LogisticsMapper.toResponse(deliveryRepository.saveAndFlush(delivery));
    }

    public void delete(Long id) {
        deliveryRepository.delete(getDelivery(id));
    }

    private Delivery getDelivery(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery", id));
    }
}
