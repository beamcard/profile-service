package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.PriceItem;
import com.beamcard.profile.domain.model.PriceType;
import java.math.BigDecimal;

public record PriceItemResponse(String name, PriceType priceType, BigDecimal amountMin, BigDecimal amountMax) {

    public static PriceItemResponse of(PriceItem item) {
        return new PriceItemResponse(item.name(), item.priceType(), item.amountMin(), item.amountMax());
    }
}
