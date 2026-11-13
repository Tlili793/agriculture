package com.awd.logistics.repository;

import com.awd.logistics.entity.Delivery;
import com.awd.logistics.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByStatusOrderByIdAsc(DeliveryStatus status);

    List<Delivery> findByOrderIdOrderByIdAsc(Long orderId);

    List<Delivery> findByStatusAndOrderIdOrderByIdAsc(DeliveryStatus status, Long orderId);
}
