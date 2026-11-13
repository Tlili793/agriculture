package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.MealRequest;
import com.workshop.refrigerator.dto.MealResponse;
import com.workshop.refrigerator.service.MealService;
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
@RequestMapping("/refrigerator/meals")
@Tag(name = "Meals", description = "CRUD operations for meal planning")
public class MealController {

    private final MealService service;

    public MealController(MealService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all meals", description = "Returns every planned meal")
    public List<MealResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a meal by ID")
    @ApiResponse(responseCode = "200", description = "Meal found")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    public MealResponse get(@Parameter(description = "Meal ID") @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new meal")
    @ApiResponse(responseCode = "201", description = "Meal created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<MealResponse> create(@Valid @RequestBody MealRequest request) {
        MealResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/meals/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing meal")
    @ApiResponse(responseCode = "200", description = "Meal updated")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public MealResponse update(@Parameter(description = "Meal ID") @PathVariable Long id,
                               @Valid @RequestBody MealRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a meal")
    @ApiResponse(responseCode = "204", description = "Meal deleted")
    @ApiResponse(responseCode = "404", description = "Meal not found")
    public ResponseEntity<Void> delete(@Parameter(description = "Meal ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
