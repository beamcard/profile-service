package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public Profile getOrProvision(UUID userId, String username) {
        return profileRepository.findByUserId(userId).orElseGet(() -> provision(userId, username));
    }

    @Override
    @Transactional
    public Profile update(UUID userId, String username, UpdateProfileCommand command) {
        Profile current = getOrProvision(userId, username);

        Profile.ProfileBuilder builder = current.toBuilder();
        if (command.displayName() != null) {
            builder.displayName(command.displayName());
        }
        if (command.bio() != null) {
            builder.bio(command.bio());
        }
        return profileRepository.save(builder.build());
    }

    private Profile provision(UUID userId, String username) {
        Profile created = profileRepository.save(
                Profile.builder().userId(userId).username(username).build());
        log.info("Provisioned profile {} for user {} (@{})", created.getId(), userId, username);
        return created;
    }
}
