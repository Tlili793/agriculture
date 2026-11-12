package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.BudgetRequest;
import com.workshop.refrigerator.dto.BudgetResponse;
import com.workshop.refrigerator.service.BudgetService;
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
@RequestMapping("/refrigerator/budgets")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @GetMapping
    public List<BudgetResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BudgetResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/budgets/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public BudgetResponse update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
