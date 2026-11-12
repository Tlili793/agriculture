package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.MealRequest;
import com.workshop.refrigerator.dto.MealResponse;
import com.workshop.refrigerator.service.MealService;
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
@RequestMapping("/refrigerator/meals")
public class MealController {

    private final MealService service;

    public MealController(MealService service) {
        this.service = service;
    }

    @GetMapping
    public List<MealResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public MealResponse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<MealResponse> create(@Valid @RequestBody MealRequest request) {
        MealResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/meals/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public MealResponse update(@PathVariable Long id, @Valid @RequestBody MealRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
