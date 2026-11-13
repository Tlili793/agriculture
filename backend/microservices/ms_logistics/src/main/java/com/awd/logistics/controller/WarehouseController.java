package com.awd.logistics.controller;

import com.awd.logistics.dto.WarehouseRequest;
import com.awd.logistics.dto.WarehouseResponse;
import com.awd.logistics.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@Tag(name = "Warehouses", description = "Manage warehouses and their storage capacities")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping
    @Operation(summary = "List all warehouses")
    @ApiResponse(responseCode = "200", description = "List of warehouses")
    public List<WarehouseResponse> findAll() {
        return warehouseService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a warehouse by id")
    @ApiResponse(responseCode = "200", description = "Warehouse found")
    @ApiResponse(responseCode = "404", description = "Warehouse not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public WarehouseResponse findById(@Parameter(description = "Warehouse id", example = "1") @PathVariable Long id) {
        return warehouseService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a warehouse")
    @ApiResponse(responseCode = "201", description = "Warehouse created")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse created = warehouseService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a warehouse")
    @ApiResponse(responseCode = "200", description = "Warehouse updated")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Warehouse not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public WarehouseResponse update(@Parameter(description = "Warehouse id", example = "1") @PathVariable Long id,
                                    @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a warehouse")
    @ApiResponse(responseCode = "204", description = "Warehouse deleted")
    @ApiResponse(responseCode = "404", description = "Warehouse not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(@Parameter(description = "Warehouse id", example = "1") @PathVariable Long id) {
        warehouseService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
