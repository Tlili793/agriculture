package esprit.marketplace.service;

import esprit.marketplace.dto.ListingRequest;
import esprit.marketplace.dto.ListingResponse;
import esprit.marketplace.entity.MarketplaceListing;
import esprit.marketplace.exception.MarketplaceAccessDeniedException;
import esprit.marketplace.exception.MarketplaceListingNotFoundException;
import esprit.marketplace.repository.MarketplaceListingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MarketplaceListingService {

    private final MarketplaceListingRepository repository;

    public MarketplaceListingService(MarketplaceListingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findAll() {
        return repository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> findByUserId(UUID userId) {
        return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ListingResponse findById(Long id) {
        return toResponse(requireListing(id));
    }

    @Transactional
    public ListingResponse create(ListingRequest request) {
        MarketplaceListing listing = new MarketplaceListing(
                request.userId(),
                request.title(),
                request.description(),
                request.price(),
                request.quantity()
        );
        return toResponse(repository.save(listing));
    }

    @Transactional
    public ListingResponse update(Long id, ListingRequest request) {
        MarketplaceListing listing = requireListing(id);
        requireOwner(listing, request.userId());

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setQuantity(request.quantity());

        return toResponse(repository.save(listing));
    }

    @Transactional
    public void delete(Long id, UUID userId) {
        MarketplaceListing listing = requireListing(id);
        requireOwner(listing, userId);
        repository.delete(listing);
    }

    private MarketplaceListing requireListing(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MarketplaceListingNotFoundException(id));
    }

    private void requireOwner(MarketplaceListing listing, UUID userId) {
        if (!listing.getUserId().equals(userId)) {
            throw new MarketplaceAccessDeniedException();
        }
    }

    private ListingResponse toResponse(MarketplaceListing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getUserId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getQuantity(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
