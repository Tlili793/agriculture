package com.awd.logistics.controller;

import com.awd.logistics.dto.DeliveryAssignmentRequest;
import com.awd.logistics.dto.DeliveryRequest;
import com.awd.logistics.dto.DeliveryResponse;
import com.awd.logistics.entity.DeliveryStatus;
import com.awd.logistics.service.DeliveryService;
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
@RequestMapping("/api/deliveries")
@Tag(name = "Deliveries", description = "Manage deliveries of orders")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    @Operation(summary = "List deliveries", description = "Optional filters: status and orderId. Ordered by id.")
    @ApiResponse(responseCode = "200", description = "List of deliveries")
    @ApiResponse(responseCode = "400", description = "Invalid filter value",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public List<DeliveryResponse> findAll(
            @Parameter(description = "Only deliveries with this status", example = "PENDING")
            @RequestParam(required = false) DeliveryStatus status,
            @Parameter(description = "Only deliveries of this order", example = "42")
            @RequestParam(required = false) Long orderId) {
        return deliveryService.findAll(status, orderId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a delivery by id")
    @ApiResponse(responseCode = "200", description = "Delivery found")
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public DeliveryResponse findById(@Parameter(description = "Delivery id", example = "1") @PathVariable Long id) {
        return deliveryService.findById(id);
    }

    @PostMapping
    @Operation(summary = "Create a delivery", description = "Status defaults to PENDING when not given.")
    @ApiResponse(responseCode = "201", description = "Delivery created")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryRequest request) {
        DeliveryResponse created = deliveryService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a delivery", description = "If the status is omitted, the current status is kept.")
    @ApiResponse(responseCode = "200", description = "Delivery updated")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public DeliveryResponse update(@Parameter(description = "Delivery id", example = "1") @PathVariable Long id,
                                   @Valid @RequestBody DeliveryRequest request) {
        return deliveryService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change the status of a delivery",
            description = "Moving to DISPATCHED also sets dispatchedAt to now if it was empty.")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "400", description = "Invalid status",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public DeliveryResponse updateStatus(@Parameter(description = "Delivery id", example = "1") @PathVariable Long id,
                                         @Parameter(description = "New status", example = "DISPATCHED", required = true)
                                         @RequestParam DeliveryStatus status) {
        return deliveryService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a delivery")
    @ApiResponse(responseCode = "204", description = "Delivery deleted")
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public ResponseEntity<Void> delete(@Parameter(description = "Delivery id", example = "1") @PathVariable Long id) {
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assignment-request")
    @Operation(summary = "Request to be assigned to a delivery",
            description = "A transporter records their own Keycloak subject as a pending assignment request.")
    @ApiResponse(responseCode = "200", description = "Assignment request recorded")
    @ApiResponse(responseCode = "400", description = "Invalid input",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Delivery already assigned or requested by another transporter",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public DeliveryResponse requestAssignment(
            @Parameter(description = "Delivery id", example = "1") @PathVariable Long id,
            @Valid @RequestBody DeliveryAssignmentRequest request) {
        return deliveryService.requestAssignment(id, request);
    }

    @PostMapping("/{id}/assignment-accept")
    @Operation(summary = "Accept the pending assignment request",
            description = "The admin assigns the delivery to the transporter who requested it.")
    @ApiResponse(responseCode = "200", description = "Assignment accepted")
    @ApiResponse(responseCode = "404", description = "Delivery not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "No pending request or delivery already assigned",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    public DeliveryResponse acceptAssignment(
            @Parameter(description = "Delivery id", example = "1") @PathVariable Long id) {
        return deliveryService.acceptAssignment(id);
    }
}
