package com.beamcard.profile.domain.service;

import static org.springframework.util.StringUtils.hasText;

import com.beamcard.profile.domain.exception.AwardNotFoundException;
import com.beamcard.profile.domain.exception.InvalidAwardException;
import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import com.beamcard.profile.domain.storage.MediaStorage.StoredObject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class AwardServiceImpl implements AwardService {

    private final AwardRepository awardRepository;
    private final ProfileService profileService;
    private final MediaStorage mediaStorage;
    private final long maxSizeBytes;
    private final List<String> allowedContentTypes;

    @Override
    @Transactional(readOnly = true)
    public List<AwardView> listForDisplay(UUID profileId) {
        return awardRepository.findByProfileId(profileId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public PresignedUpload requestUpload(UUID userId, String username, String contentType) {
        requireAllowedType(contentType);
        UUID profileId = profileService.getOrProvision(userId, username).getId();
        String extension = contentType.substring(contentType.indexOf('/') + 1);
        String key = "awards/%s/%s.%s".formatted(profileId, UUID.randomUUID(), extension);
        return mediaStorage.presignUpload(key, contentType);
    }

    @Override
    @Transactional
    public AwardView create(UUID userId, String key) {
        UUID profileId = profileService.getByUserId(userId).getId();
        if (!key.startsWith("awards/" + profileId + "/")) {
            throw new InvalidAwardException("The image key does not belong to your profile.");
        }
        StoredObject object = mediaStorage
                .head(key)
                .orElseThrow(() -> new InvalidAwardException("No uploaded file was found for that key."));
        if (object.contentLength() > maxSizeBytes) {
            throw new InvalidAwardException("The image exceeds the maximum allowed size.");
        }
        requireAllowedType(object.contentType());

        int position = awardRepository.findByProfileId(profileId).size() + 1;
        Award created = awardRepository.save(Award.builder()
                .profileId(profileId)
                .imageKey(key)
                .position(position)
                .build());
        log.info("Added award {} to profile {} at position {}", created.getId(), profileId, position);
        return toView(created);
    }

    @Override
    @Transactional
    public AwardView update(UUID userId, UUID awardId, String description) {
        UUID profileId = profileService.getByUserId(userId).getId();
        Award award = getOwnedAward(profileId, awardId);
        Award saved = awardRepository.save(award.toBuilder()
                .description(hasText(description) ? description : null)
                .build());
        return toView(saved);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID awardId) {
        UUID profileId = profileService.getByUserId(userId).getId();
        Award award = getOwnedAward(profileId, awardId);
        awardRepository.deleteById(awardId);
        mediaStorage.delete(award.getImageKey());
        awardRepository.saveAll(recompactAwards(awardRepository.findByProfileId(profileId)));
    }

    @Override
    @Transactional
    public List<AwardView> reorder(UUID userId, List<UUID> orderedIds) {
        UUID profileId = profileService.getByUserId(userId).getId();
        Map<UUID, Award> byId = new LinkedHashMap<>();
        for (Award award : awardRepository.findByProfileId(profileId)) {
            byId.put(award.getId(), award);
        }

        List<Award> result = new ArrayList<>(byId.size());
        int position = 1;
        for (UUID id : orderedIds) {
            Award award = byId.remove(id);
            if (Objects.isNull(award)) {
                throw new AwardNotFoundException(id);
            }
            result.add(award.toBuilder().position(position++).build());
        }
        for (Award award : byId.values()) {
            result.add(award.toBuilder().position(position++).build());
        }
        return awardRepository.saveAll(result).stream().map(this::toView).toList();
    }

    private AwardView toView(Award award) {
        return new AwardView(award, mediaStorage.publicUrl(award.getImageKey()));
    }

    private Award getOwnedAward(UUID profileId, UUID awardId) {
        Award award = awardRepository.findById(awardId).orElseThrow(() -> new AwardNotFoundException(awardId));
        if (!profileId.equals(award.getProfileId())) {
            throw new AwardNotFoundException(awardId);
        }
        return award;
    }

    private void requireAllowedType(String contentType) {
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new InvalidAwardException("Unsupported image type: " + contentType);
        }
    }

    private static List<Award> recompactAwards(List<Award> ordered) {
        List<Award> recompacted = new ArrayList<>(ordered.size());
        IntStream.range(0, ordered.size()).forEach(i -> {
            int position = i + 1;
            Award award = ordered.get(i);
            recompacted.add(
                    award.getPosition() == position
                            ? award
                            : award.toBuilder().position(position).build());
        });
        return recompacted;
    }
}
