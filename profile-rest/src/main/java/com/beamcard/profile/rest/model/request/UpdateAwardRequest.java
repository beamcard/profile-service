package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.Size;

public record UpdateAwardRequest(@Size(max = 300) String description) {}
