package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

import java.util.List;

public record Affiliation(
        String role, String organization, String address, String description, List<OpeningHours> openingHours) {

    public Affiliation(String role, String organization, String address, String description) {
        this(role, organization, address, description, List.of());
    }

    public boolean isEmpty() {
        return !hasText(role)
                && !hasText(organization)
                && !hasText(address)
                && !hasText(description)
                && (openingHours == null || openingHours.isEmpty());
    }
}
