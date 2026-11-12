package esprit.marketplace.controller;

import esprit.marketplace.dto.ListingRequest;
import esprit.marketplace.dto.ListingResponse;
import esprit.marketplace.service.MarketplaceListingService;
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
public class MarketplaceListingController {

    private final MarketplaceListingService service;

    public MarketplaceListingController(MarketplaceListingService service) {
        this.service = service;
    }

    @GetMapping
    public List<ListingResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ListingResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<ListingResponse> findByUserId(@PathVariable UUID userId) {
        return service.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody ListingRequest request) {
        ListingResponse response = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{id}")
    public ListingResponse update(@PathVariable Long id, @Valid @RequestBody ListingRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam UUID userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
