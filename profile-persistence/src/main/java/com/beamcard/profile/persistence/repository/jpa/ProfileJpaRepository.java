package com.beamcard.profile.persistence.repository.jpa;

import com.beamcard.profile.persistence.model.ProfileJpa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpa, UUID> {

    Optional<ProfileJpa> findByUserId(UUID userId);

    Optional<ProfileJpa> findByUsername(String username);
}
