package com.beamcard.profile.rest.validation;

import com.beamcard.profile.domain.model.LinkType;

public interface LinkUrlForm {

    String url();

    LinkType type();
}
