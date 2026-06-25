package com.beamcard.profile.rest.model.request;

import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.rest.validation.LinkUrlForm;
import com.beamcard.profile.rest.validation.ValidLink;
import jakarta.validation.constraints.Size;

@ValidLink
public record UpdateLinkRequest(@Size(max = 80) String label, @Size(max = 2048) String url, LinkType type)
        implements LinkUrlForm {}
