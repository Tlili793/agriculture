package esprit.marketplace.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ListingResponse(
        Long id,
        UUID userId,
        String title,
        String description,
        BigDecimal price,
        Integer quantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
