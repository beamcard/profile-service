package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

import java.math.BigDecimal;

public record PriceItem(String name, PriceType priceType, BigDecimal amountMin, BigDecimal amountMax) {

    public boolean isEmpty() {
        return !hasText(name);
    }
}
