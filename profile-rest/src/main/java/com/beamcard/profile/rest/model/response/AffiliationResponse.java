package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Affiliation;

public record AffiliationResponse(String role, String organization, String address, String description) {

    public static AffiliationResponse of(Affiliation affiliation) {
        return new AffiliationResponse(
                affiliation.role(), affiliation.organization(), affiliation.address(), affiliation.description());
    }
}
