package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.ActivityJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityJpaRepository extends JpaRepository<ActivityJpa, UUID> {

    List<ActivityJpa> findByProfileIdOrderByPositionAsc(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
