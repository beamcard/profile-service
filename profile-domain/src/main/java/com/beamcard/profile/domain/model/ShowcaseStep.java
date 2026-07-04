package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

public record ShowcaseStep(String imageKey, String description) {

    public boolean hasImage() {
        return hasText(imageKey);
    }
}
