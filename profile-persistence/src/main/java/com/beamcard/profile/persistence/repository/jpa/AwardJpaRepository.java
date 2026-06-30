package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.AwardJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AwardJpaRepository extends JpaRepository<AwardJpa, UUID> {

    List<AwardJpa> findByProfileIdOrderByPositionAsc(UUID profileId);
}
