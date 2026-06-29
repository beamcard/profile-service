package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

public record Location(String country, String city) {

    public boolean isEmpty() {
        return !hasText(country) && !hasText(city);
    }
}
