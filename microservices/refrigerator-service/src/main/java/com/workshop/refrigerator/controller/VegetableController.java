package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.service.VegetableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/refrigerator/vegetables")
public class VegetableController {

    private final VegetableService service;

    public VegetableController(VegetableService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProduceResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProduceResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<ProduceResponse> create(@Valid @RequestBody ProduceRequest request) {
        ProduceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/vegetables/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public ProduceResponse update(@PathVariable Long id, @Valid @RequestBody ProduceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
