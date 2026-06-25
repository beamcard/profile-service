package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import java.util.List;
import java.util.UUID;

public interface LinkService {

    List<Link> listByProfileId(UUID profileId);

    Link create(UUID userId, String username, CreateLinkCommand command);

    Link update(UUID userId, UUID linkId, UpdateLinkCommand command);

    void delete(UUID userId, UUID linkId);

    List<Link> reorder(UUID userId, List<UUID> orderedIds);

    record CreateLinkCommand(String label, String url, LinkType type) {}

    record UpdateLinkCommand(String label, String url, LinkType type) {}
}
