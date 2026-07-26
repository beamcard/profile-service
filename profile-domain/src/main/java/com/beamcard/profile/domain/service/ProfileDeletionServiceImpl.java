package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.domain.repository.ShowcaseRepository;
import com.beamcard.profile.domain.storage.MediaStorage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Slf4j
public class ProfileDeletionServiceImpl implements ProfileDeletionService {

    private final ProfileRepository profileRepository;
    private final AwardRepository awardRepository;
    private final ShowcaseRepository showcaseRepository;
    private final MediaStorage mediaStorage;

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId).orElse(null);
        if (profile == null) {
            return;
        }
        UUID profileId = profile.getId();

        deleteMedia(profile.getAvatarKey());
        awardRepository.findByProfileId(profileId).forEach(award -> deleteMedia(award.getImageKey()));
        showcaseRepository.findByProfileId(profileId).stream()
                .flatMap(showcase -> showcase.stepsOrEmpty().stream())
                .forEach(step -> deleteMedia(step.imageKey()));

        profileRepository.deleteByUserId(userId);
        log.info("Deleted profile {} and all media for user {}", profileId, userId);
    }

    private void deleteMedia(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        try {
            mediaStorage.delete(key);
        } catch (RuntimeException e) {
            log.warn("Could not delete media object {} during profile deletion", key, e);
        }
    }
}
