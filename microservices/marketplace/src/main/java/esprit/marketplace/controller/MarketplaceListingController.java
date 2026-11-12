package esprit.marketplace.controller;

import esprit.marketplace.dto.ListingRequest;
import esprit.marketplace.dto.ListingResponse;
import esprit.marketplace.service.MarketplaceListingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/mic3/marketplace/listings")
@Tag(name = "Marketplace Listings", description = "CRUD operations for agricultural marketplace listings")
public class MarketplaceListingController {

    private final MarketplaceListingService service;

    public MarketplaceListingController(MarketplaceListingService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all marketplace listings")
    public List<ListingResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a listing by ID")
    @ApiResponse(responseCode = "200", description = "Listing found")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ListingResponse findById(@Parameter(description = "Listing ID") @PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "List all listings for a user")
    public List<ListingResponse> findByUserId(@Parameter(description = "User UUID") @PathVariable UUID userId) {
        return service.findByUserId(userId);
    }

    @PostMapping
    @Operation(summary = "Create a new listing")
    @ApiResponse(responseCode = "201", description = "Listing created")
    @ApiResponse(responseCode = "400", description = "Validation error")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody ListingRequest request) {
        ListingResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a listing")
    @ApiResponse(responseCode = "200", description = "Listing updated")
    @ApiResponse(responseCode = "403", description = "Not the listing owner")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ListingResponse update(@Parameter(description = "Listing ID") @PathVariable Long id,
                                  @Valid @RequestBody ListingRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a listing")
    @ApiResponse(responseCode = "204", description = "Listing deleted")
    @ApiResponse(responseCode = "403", description = "Not the listing owner")
    @ApiResponse(responseCode = "404", description = "Listing not found")
    public ResponseEntity<Void> delete(@Parameter(description = "Listing ID") @PathVariable Long id,
                                       @Parameter(description = "User UUID of the owner") @RequestParam UUID userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
