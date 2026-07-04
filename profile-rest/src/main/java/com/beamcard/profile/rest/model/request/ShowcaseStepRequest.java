package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShowcaseStepRequest(@NotBlank String imageKey, @Size(max = 300) String description) {}
