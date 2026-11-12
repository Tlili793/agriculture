package com.agri.reclamation.repository;

import com.agri.reclamation.entity.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByConsumerId(Long consumerId);
}
