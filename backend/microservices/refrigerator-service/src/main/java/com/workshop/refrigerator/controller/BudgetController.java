package com.workshop.refrigerator.controller;

import com.workshop.refrigerator.dto.BudgetRequest;
import com.workshop.refrigerator.dto.BudgetResponse;
import com.workshop.refrigerator.service.BudgetService;
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
@RequestMapping("/refrigerator/budgets")
@Tag(name = "Budgets", description = "Monthly budget management")
public class BudgetController {

    private final BudgetService service;

    public BudgetController(BudgetService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all budgets", description = "Returns every monthly budget")
    public List<BudgetResponse> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a budget by ID")
    @ApiResponse(responseCode = "200", description = "Budget found")
    @ApiResponse(responseCode = "404", description = "Budget not found")
    public BudgetResponse get(@Parameter(description = "Budget ID") @PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a new monthly budget")
    @ApiResponse(responseCode = "201", description = "Budget created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "409", description = "Budget for this month/year already exists")
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody BudgetRequest request) {
        BudgetResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/refrigerator/budgets/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing budget")
    @ApiResponse(responseCode = "200", description = "Budget updated")
    @ApiResponse(responseCode = "404", description = "Budget not found")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public BudgetResponse update(@Parameter(description = "Budget ID") @PathVariable Long id,
                                 @Valid @RequestBody BudgetRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget")
    @ApiResponse(responseCode = "204", description = "Budget deleted")
    @ApiResponse(responseCode = "404", description = "Budget not found")
    public ResponseEntity<Void> delete(@Parameter(description = "Budget ID") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
