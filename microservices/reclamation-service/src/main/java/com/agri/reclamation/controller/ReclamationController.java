package com.agri.reclamation.controller;

import com.agri.reclamation.dto.CreateReclamationRequest;
import com.agri.reclamation.dto.UpdateStatusRequest;
import com.agri.reclamation.entity.Reclamation;
import com.agri.reclamation.service.ReclamationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reclamations")
public class ReclamationController {

    private final ReclamationService service;

    public ReclamationController(ReclamationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)                       // 201
    public Reclamation create(@Valid @RequestBody CreateReclamationRequest req) {
        return service.create(req);
    }

    @GetMapping                                               // 200
    public List<Reclamation> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")                                      // 200 or 404
    public Reclamation findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/consumer/{consumerId}")                     // 200
    public List<Reclamation> findByConsumer(@PathVariable Long consumerId) {
        return service.findByConsumer(consumerId);
    }

    @PutMapping("/{id}/status")                               // 200, 404 or 409
    public Reclamation updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        return service.updateStatus(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)                    // 204 or 404
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
