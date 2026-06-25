package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import java.util.UUID;

public record LinkResponse(UUID id, String label, String url, LinkType type, int position) {

    public static LinkResponse of(Link link) {
        return new LinkResponse(link.getId(), link.getLabel(), link.getUrl(), link.getType(), link.getPosition());
    }
}
