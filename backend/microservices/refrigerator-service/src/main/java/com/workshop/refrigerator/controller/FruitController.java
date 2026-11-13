package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.ProduceRequest;
import com.workshop.refrigerator.dto.ProduceResponse;
import com.workshop.refrigerator.service.FruitService;
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
@RequestMapping("/refrigerator/fruits")
@Tag(name = "Fruits", description = "CRUD operations for fruit inventory")
public class FruitController {

    private final FruitService service;

    public FruitController(FruitService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all fruits", description = "Returns every fruit in the inventory")
    public List<ProduceResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a fruit by ID")
    @ApiResponse(responseCode = "200", description = "Fruit found")
    @ApiResponse(responseCode = "404", description = "Fruit not found")
    public ProduceResponse get(@Parameter(description = "Fruit ID") @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Add a new fruit")
    @ApiResponse(responseCode = "201", description = "Fruit created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ProduceResponse> create(@Valid @RequestBody ProduceRequest request) {
        ProduceResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/fruits/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing fruit")
    @ApiResponse(responseCode = "200", description = "Fruit updated")
    @ApiResponse(responseCode = "404", description = "Fruit not found")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ProduceResponse update(@Parameter(description = "Fruit ID") @PathVariable Long id,
                                  @Valid @RequestBody ProduceRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a fruit")
    @ApiResponse(responseCode = "204", description = "Fruit deleted")
    @ApiResponse(responseCode = "404", description = "Fruit not found")
    public ResponseEntity<Void> delete(@Parameter(description = "Fruit ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
