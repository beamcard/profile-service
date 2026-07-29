package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Affiliation;
import java.util.List;

public record AffiliationResponse(
        String role, String organization, String address, String description, List<OpeningHoursResponse> openingHours) {

    public static AffiliationResponse of(Affiliation affiliation) {
        List<OpeningHoursResponse> hours = affiliation.openingHours() == null
                ? List.of()
                : affiliation.openingHours().stream()
                        .map(OpeningHoursResponse::of)
                        .toList();
        return new AffiliationResponse(
                affiliation.role(),
                affiliation.organization(),
                affiliation.address(),
                affiliation.description(),
                hours);
    }
}
