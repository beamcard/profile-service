package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.Size;

public record AffiliationRequest(
        @Size(max = 80) String role,
        @Size(max = 120) String organization,
        @Size(max = 200) String address,
        @Size(max = 300) String description) {}
