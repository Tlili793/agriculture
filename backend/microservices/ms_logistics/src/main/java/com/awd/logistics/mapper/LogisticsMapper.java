package com.awd.logistics.mapper;

import com.awd.logistics.dto.DeliveryRequest;
import com.awd.logistics.dto.DeliveryResponse;
import com.awd.logistics.dto.WarehouseRequest;
import com.awd.logistics.dto.WarehouseResponse;
import com.awd.logistics.entity.Delivery;
import com.awd.logistics.entity.DeliveryStatus;
import com.awd.logistics.entity.Warehouse;

/** Converts between entities and DTOs. */
public final class LogisticsMapper {

    private LogisticsMapper() {
    }

    // ---------- Delivery ----------

    public static Delivery toEntity(DeliveryRequest request) {
        DeliveryStatus status = request.status() != null ? request.status() : DeliveryStatus.PENDING;
        return new Delivery(request.orderId(), status, request.dispatchedAt(), request.destination());
    }

    public static void updateEntity(Delivery delivery, DeliveryRequest request) {
        delivery.setOrderId(request.orderId());
        if (request.status() != null) {
            delivery.setStatus(request.status());
        }
        delivery.setDispatchedAt(request.dispatchedAt());
        delivery.setDestination(request.destination());
    }

    public static DeliveryResponse toResponse(Delivery delivery) {
        return new DeliveryResponse(delivery.getId(), delivery.getOrderId(), delivery.getStatus(),
                delivery.getDispatchedAt(), delivery.getDestination(), delivery.getRequestedTransporterId(),
                delivery.getAssignmentRequestedAt(), delivery.getAssignedTransporterId(), delivery.getAssignedAt());
    }

    // ---------- Warehouse ----------

    public static Warehouse toEntity(WarehouseRequest request) {
        return new Warehouse(request.name(), request.fridgeCapacity(), request.normalCapacity(),
                request.addressLocale());
    }

    public static void updateEntity(Warehouse warehouse, WarehouseRequest request) {
        warehouse.setName(request.name());
        warehouse.setFridgeCapacity(request.fridgeCapacity());
        warehouse.setNormalCapacity(request.normalCapacity());
        warehouse.setAddressLocale(request.addressLocale());
    }

    public static WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(warehouse.getId(), warehouse.getName(), warehouse.getFridgeCapacity(),
                warehouse.getNormalCapacity(), warehouse.getAddressLocale());
    }
}
