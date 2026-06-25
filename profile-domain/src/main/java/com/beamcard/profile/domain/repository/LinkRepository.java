package com.beamcard.profile.domain.repository;

import com.beamcard.profile.domain.model.Link;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository {

    List<Link> findByProfileId(UUID profileId);

    Optional<Link> findById(UUID id);

    Link save(Link link);

    List<Link> saveAll(List<Link> links);

    void deleteById(UUID id);
}
