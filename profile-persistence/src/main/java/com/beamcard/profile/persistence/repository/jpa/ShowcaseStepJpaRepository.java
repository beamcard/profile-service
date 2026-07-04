package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.ShowcaseStepJpa;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowcaseStepJpaRepository extends JpaRepository<ShowcaseStepJpa, UUID> {

    List<ShowcaseStepJpa> findByShowcaseIdOrderByPositionAsc(UUID showcaseId);
}
