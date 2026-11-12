package com.agri.reclamation.repository;

import com.agri.reclamation.entity.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByUserId(UUID userId);
}
