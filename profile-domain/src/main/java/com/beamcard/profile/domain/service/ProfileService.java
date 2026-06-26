package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Profile;
import java.util.UUID;

public interface ProfileService {

    Profile getOrProvision(UUID userId, String username);

    Profile getByUserId(UUID userId);

    Profile getByUsername(String username);

    Profile update(UUID userId, String username, UpdateProfileCommand command);

    Profile setAvatar(UUID userId, String avatarKey);

    record UpdateProfileCommand(String displayName, String bio) {}
}
