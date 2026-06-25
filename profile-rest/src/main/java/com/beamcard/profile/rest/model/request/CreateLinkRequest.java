package com.beamcard.profile.rest.model.request;

import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.rest.validation.LinkUrlForm;
import com.beamcard.profile.rest.validation.ValidLink;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@ValidLink
public record CreateLinkRequest(
        @NotBlank @Size(max = 80) String label, @NotBlank @Size(max = 2048) String url, @NotNull LinkType type)
        implements LinkUrlForm {}
