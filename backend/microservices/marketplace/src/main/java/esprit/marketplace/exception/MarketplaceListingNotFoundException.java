package esprit.marketplace.exception;

public class MarketplaceListingNotFoundException extends RuntimeException {

    public MarketplaceListingNotFoundException(Long id) {
        super("Marketplace listing " + id + " was not found");
    }
}
