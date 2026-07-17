package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.PriceItemJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceItemJpaRepository extends JpaRepository<PriceItemJpa, UUID> {

    List<PriceItemJpa> findByProfileIdOrderByPositionAsc(UUID profileId);

    void deleteByProfileId(UUID profileId);
}
