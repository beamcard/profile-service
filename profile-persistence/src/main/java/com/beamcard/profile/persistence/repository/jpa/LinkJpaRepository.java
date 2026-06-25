package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.LinkJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkJpaRepository extends JpaRepository<LinkJpa, UUID> {

    List<LinkJpa> findByProfileIdOrderByPositionAsc(UUID profileId);
}
