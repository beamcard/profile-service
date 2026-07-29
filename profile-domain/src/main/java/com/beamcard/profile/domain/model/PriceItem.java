package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

import java.math.BigDecimal;

public record PriceItem(
        String name, PriceType priceType, BigDecimal amountMin, BigDecimal amountMax, Integer durationMinutes) {

    public PriceItem(String name, PriceType priceType, BigDecimal amountMin, BigDecimal amountMax) {
        this(name, priceType, amountMin, amountMax, null);
    }

    public boolean isEmpty() {
        return !hasText(name);
    }
}
