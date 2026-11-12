package esprit.marketplace.repository;

import esprit.marketplace.entity.MarketplaceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, Long> {

    List<MarketplaceListing> findAllByOrderByCreatedAtDesc();

    List<MarketplaceListing> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
}
