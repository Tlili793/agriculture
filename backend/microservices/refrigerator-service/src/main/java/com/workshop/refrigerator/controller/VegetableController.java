package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.service.VegetableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Vegetables", description = "CRUD operations for vegetable inventory")
public class VegetableController {

    private final VegetableService service;

    public VegetableController(VegetableService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all vegetables", description = "Returns every vegetable in the inventory")
    public List<ProduceResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a vegetable by ID")
    @ApiResponse(responseCode = "200", description = "Vegetable found")
    @ApiResponse(responseCode = "404", description = "Vegetable not found")
    public ProduceResponse get(@Parameter(description = "Vegetable ID") @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Add a new vegetable")
    @ApiResponse(responseCode = "201", description = "Vegetable created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ProduceResponse> create(@Valid @RequestBody ProduceRequest request) {
        ProduceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/vegetables/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing vegetable")
    @ApiResponse(responseCode = "200", description = "Vegetable updated")
    @ApiResponse(responseCode = "404", description = "Vegetable not found")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ProduceResponse update(@Parameter(description = "Vegetable ID") @PathVariable Long id,
                                  @Valid @RequestBody ProduceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vegetable")
    @ApiResponse(responseCode = "204", description = "Vegetable deleted")
    @ApiResponse(responseCode = "404", description = "Vegetable not found")
    public ResponseEntity<Void> delete(@Parameter(description = "Vegetable ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
