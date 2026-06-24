package com.beamcard.profile.domain.repository;

import com.beamcard.profile.domain.model.Profile;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository {

    Optional<Profile> findByUserId(UUID userId);

    Optional<Profile> findByUsername(String username);

    Profile save(Profile profile);
}
