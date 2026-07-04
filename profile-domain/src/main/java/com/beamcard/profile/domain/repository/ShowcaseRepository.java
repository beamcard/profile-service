package com.beamcard.profile.domain.repository;

import com.beamcard.profile.domain.model.Showcase;
import java.util.List;
import java.util.UUID;

public interface ShowcaseRepository {

    List<Showcase> findByProfileId(UUID profileId);

    void replace(UUID profileId, List<Showcase> showcases);
}
