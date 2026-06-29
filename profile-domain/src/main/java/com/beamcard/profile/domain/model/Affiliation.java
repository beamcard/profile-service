package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

public record Affiliation(String role, String organization, String address, String description) {

    public boolean isEmpty() {
        return !hasText(role) && !hasText(organization) && !hasText(address) && !hasText(description);
    }
}
