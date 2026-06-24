package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Profile;
import java.time.Instant;
import java.util.UUID;

public record ProfileResponse(
        UUID id, String username, String displayName, String bio, Instant createdAt, Instant updatedAt) {

    public static ProfileResponse of(Profile p) {
        return new ProfileResponse(
                p.getId(), p.getUsername(), p.getDisplayName(), p.getBio(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
