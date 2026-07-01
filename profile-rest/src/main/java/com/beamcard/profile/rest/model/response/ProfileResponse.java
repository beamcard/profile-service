package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Affiliation;
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
        LocationResponse location,
        List<AffiliationResponse> affiliations,
        String avatarUrl,
        Instant createdAt,
        Instant updatedAt,
        List<LinkResponse> links,
        List<AwardResponse> awards,
        String locale) {

    public static ProfileResponse of(Profile profile, List<Link> links, String avatarUrl, List<AwardResponse> awards) {
        List<Affiliation> affiliations = profile.getAffiliations() == null ? List.of() : profile.getAffiliations();
        return new ProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getDisplayName(),
                profile.getBio(),
                LocationResponse.of(profile.getLocation()),
                affiliations.stream().map(AffiliationResponse::of).toList(),
                avatarUrl,
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                links.stream().map(LinkResponse::of).toList(),
                awards,
                profile.getLocale());
    }
}
