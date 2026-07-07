package com.beamcard.profile.rest.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateProfileRequest(
        @Size(max = 80) String displayName,
        @Size(max = 500) String bio,
        @Pattern(regexp = "^(\\+[1-9]\\d{7,14})?$", message = "invalid_phone") String phone,
        @Valid LocationRequest location,
        @Valid List<AffiliationRequest> affiliations,
        List<@Size(max = 25) String> activities) {}
