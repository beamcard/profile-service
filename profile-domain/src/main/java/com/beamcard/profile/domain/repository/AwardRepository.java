package com.beamcard.profile.domain.repository;

import com.beamcard.profile.domain.model.Award;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AwardRepository {

    List<Award> findByProfileId(UUID profileId);

    Optional<Award> findById(UUID id);

    Award save(Award award);

    List<Award> saveAll(List<Award> awards);

    void deleteById(UUID id);
}
