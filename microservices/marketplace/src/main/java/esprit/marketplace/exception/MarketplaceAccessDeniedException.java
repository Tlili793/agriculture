package esprit.marketplace.exception;

public class MarketplaceAccessDeniedException extends RuntimeException {

    public MarketplaceAccessDeniedException() {
        super("Marketplace listing does not belong to the authenticated user");
    }
}
