package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import java.util.List;
import java.util.UUID;

public interface ProfileService {

    Profile getOrProvision(UUID userId, String username);

    Profile getOrProvision(UUID userId, String username, String locale);

    Profile getByUserId(UUID userId);

    Profile getByUsername(String username);

    Profile update(UUID userId, String username, UpdateProfileCommand command);

    Profile setAvatar(UUID userId, String avatarKey);

    record UpdateProfileCommand(
            String displayName,
            String bio,
            Location location,
            List<Affiliation> affiliations,
            List<String> activities) {}
}
