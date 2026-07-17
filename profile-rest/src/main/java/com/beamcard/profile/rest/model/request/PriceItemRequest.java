package com.beamcard.profile.rest.model.request;

import com.beamcard.profile.domain.model.PriceType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PriceItemRequest(
        @NotBlank @Size(max = 500) String name,
        @NotNull PriceType priceType,
        @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal amountMin,
        @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) BigDecimal amountMax) {

    @AssertTrue(message = "invalid_price_amounts") public boolean isAmountsConsistent() {
        if (priceType == null) {
            return true;
        }
        return switch (priceType) {
            case EXACT, FROM -> amountMin != null && amountMax == null;
            case RANGE -> amountMin != null && amountMax != null && amountMax.compareTo(amountMin) >= 0;
        };
    }
}
