package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.Profile;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String username,
        String displayName,
        String bio,
        Instant createdAt,
        Instant updatedAt,
        List<LinkResponse> links) {

    public static ProfileResponse of(Profile profile, List<Link> links) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                links.stream().map(LinkResponse::of).toList());
    }
}
