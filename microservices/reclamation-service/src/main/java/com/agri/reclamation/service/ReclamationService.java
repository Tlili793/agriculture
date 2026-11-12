package com.agri.reclamation.service;

import com.agri.reclamation.dto.CreateReclamationRequest;
import com.agri.reclamation.dto.UpdateStatusRequest;
import com.agri.reclamation.entity.Reclamation;
import com.agri.reclamation.entity.ReclamationStatus;
import com.agri.reclamation.repository.ReclamationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReclamationService {

    private final ReclamationRepository repository;

    public ReclamationService(ReclamationRepository repository) {
        this.repository = repository;
    }

    public Reclamation create(CreateReclamationRequest req) {
        // SESSION 6 (sync): call Ordering with OpenFeign -> GET /api/orders/{orderId}
        //                   to check that the order exists before saving.
        Reclamation r = new Reclamation();
        r.setConsumerId(req.consumerId());
        r.setOrderId(req.orderId());
        r.setSubject(req.subject());
        r.setDescription(req.description());
        return repository.save(r);
    }

    public List<Reclamation> findAll() {
        return repository.findAll();
    }

    public List<Reclamation> findByConsumer(Long consumerId) {
        return repository.findByConsumerId(consumerId);
    }

    public Reclamation findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reclamation not found"));
    }

    public Reclamation updateStatus(Long id, UpdateStatusRequest req) {
        Reclamation r = findById(id);
        // Simple rule: once RESOLVED or REJECTED, it cannot change any more.
        if (r.getStatus() == ReclamationStatus.RESOLVED || r.getStatus() == ReclamationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reclamation already closed");
        }
        r.setStatus(req.status());
        r.setAdminResponse(req.adminResponse());
        // SESSION 7 (async): if RESOLVED, publish ReclamationResolvedEvent to RabbitMQ
        //                    so Ordering can refund the order.
        return repository.save(r);
    }

    public void delete(Long id) {
        repository.delete(findById(id));
    }
}
