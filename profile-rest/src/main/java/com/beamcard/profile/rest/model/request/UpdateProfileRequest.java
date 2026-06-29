package com.beamcard.profile.rest.model.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 80) String displayName, @Size(max = 500) String bio, @Valid LocationRequest location) {}
