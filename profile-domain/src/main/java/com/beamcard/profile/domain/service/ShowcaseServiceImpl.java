package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.exception.InvalidShowcaseException;
import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.model.ShowcaseStep;
import com.beamcard.profile.domain.repository.ShowcaseRepository;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import com.beamcard.profile.domain.storage.MediaStorage.StoredObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@RequiredArgsConstructor
public class ShowcaseServiceImpl implements ShowcaseService {

    private static final String KEY_ROOT = "showcases/";

    private final ShowcaseRepository showcaseRepository;
    private final ProfileService profileService;
    private final MediaStorage mediaStorage;
    private final List<String> allowedContentTypes;
    private final long maxSizeBytes;
    private final int maxStepsPerShowcase;

    @Override
    @Transactional(readOnly = true)
    public List<ShowcaseView> listForDisplay(UUID profileId) {
        return showcaseRepository.findByProfileId(profileId).stream()
                .map(this::toView)
                .toList();
    }

    @Override
    @Transactional
    public PresignedUpload requestUpload(UUID userId, String username, String contentType) {
        validateAllowedType(contentType);
        UUID profileId = profileService.getOrProvision(userId, username).getId();
        String extension = contentType.substring(contentType.indexOf('/') + 1);
        String key = "%s%s/%s.%s".formatted(KEY_ROOT, profileId, UUID.randomUUID(), extension);
        return mediaStorage.presignUpload(key, contentType);
    }

    @Override
    @Transactional
    public List<ShowcaseView> replace(UUID userId, List<Showcase> incoming) {
        UUID profileId = profileService.getByUserId(userId).getId();
        String keyPrefix = KEY_ROOT + profileId + "/";
        List<Showcase> sanitized = sanitize(incoming, keyPrefix);

        Set<String> oldKeys = imageKeysOf(showcaseRepository.findByProfileId(profileId));
        Set<String> newKeys = imageKeysOf(sanitized);
        validateNewImages(newKeys, oldKeys);

        showcaseRepository.replace(profileId, sanitized);
        deleteOrphanedImages(oldKeys, newKeys);
        return sanitized.stream().map(this::toView).toList();
    }

    private void deleteOrphanedImages(Set<String> oldKeys, Set<String> newKeys) {
        oldKeys.stream().filter(key -> !newKeys.contains(key)).forEach(key -> {
            try {
                mediaStorage.delete(key);
            } catch (RuntimeException e) {
                log.warn("Failed to delete orphaned showcase image {}; leaving it in storage", key, e);
            }
        });
    }

    private void validateNewImages(Set<String> newKeys, Set<String> oldKeys) {
        newKeys.stream().filter(key -> !oldKeys.contains(key)).forEach(this::validateImage);
    }

    private void validateImage(String key) {
        StoredObject object = mediaStorage
                .head(key)
                .orElseThrow(() -> new InvalidShowcaseException("No uploaded image was found for a step."));
        if (object.contentLength() > maxSizeBytes) {
            throw new InvalidShowcaseException("An image exceeds the maximum allowed size.");
        }
    }

    private List<Showcase> sanitize(List<Showcase> incoming, String keyPrefix) {
        if (incoming == null) {
            return List.of();
        }
        List<Showcase> result = new ArrayList<>();
        for (Showcase showcase : incoming) {
            if (showcase != null) {
                List<ShowcaseStep> steps = sanitizeSteps(showcase, keyPrefix);
                if (!steps.isEmpty()) {
                    if (steps.size() > maxStepsPerShowcase) {
                        throw new InvalidShowcaseException(
                                "A showcase can have at most " + maxStepsPerShowcase + " steps");
                    }
                    result.add(new Showcase(blankToNull(showcase.title()), blankToNull(showcase.intro()), steps));
                }
            }
        }
        return result;
    }

    private List<ShowcaseStep> sanitizeSteps(Showcase showcase, String keyPrefix) {
        List<ShowcaseStep> steps = new ArrayList<>();
        for (ShowcaseStep step : showcase.stepsOrEmpty()) {
            if (step != null && step.hasImage()) {
                String key = step.imageKey().trim();
                if (!key.startsWith(keyPrefix)) {
                    throw new InvalidShowcaseException("An image does not belong to your profile.");
                }
                steps.add(new ShowcaseStep(key, blankToNull(step.description())));
            }
        }
        return steps;
    }

    private ShowcaseView toView(Showcase showcase) {
        List<StepView> steps = showcase.stepsOrEmpty().stream()
                .map(step -> new StepView(step.imageKey(), mediaStorage.publicUrl(step.imageKey()), step.description()))
                .toList();
        return new ShowcaseView(showcase.title(), showcase.intro(), steps);
    }

    private static Set<String> imageKeysOf(List<Showcase> showcases) {
        return showcases.stream()
                .flatMap(showcase -> showcase.stepsOrEmpty().stream())
                .map(ShowcaseStep::imageKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.toSet());
    }

    private void validateAllowedType(String contentType) {
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new InvalidShowcaseException("Unsupported image type: " + contentType);
        }
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
