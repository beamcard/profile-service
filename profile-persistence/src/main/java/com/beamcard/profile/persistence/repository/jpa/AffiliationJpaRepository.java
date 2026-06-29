package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.AffiliationJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AffiliationJpaRepository extends JpaRepository<AffiliationJpa, UUID> {

    List<AffiliationJpa> findByProfileIdOrderByPositionAsc(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
