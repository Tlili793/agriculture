package esprit.marketplace.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record ListingRequest(
        @NotNull UUID userId,
        @NotBlank @Size(max = 150) String title,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.0") @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @PositiveOrZero Integer quantity
) {
}
