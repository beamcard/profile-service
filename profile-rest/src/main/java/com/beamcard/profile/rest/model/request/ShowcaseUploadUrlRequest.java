package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.NotBlank;

public record ShowcaseUploadUrlRequest(@NotBlank String contentType) {}
