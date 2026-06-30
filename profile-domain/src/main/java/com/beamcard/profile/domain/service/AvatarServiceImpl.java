package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.exception.InvalidAvatarException;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import com.beamcard.profile.domain.storage.MediaStorage.StoredObject;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {

    private final ProfileService profileService;
    private final MediaStorage avatarStorage;
    private final long maxSizeBytes;
    private final List<String> allowedContentTypes;

    @Override
    @Transactional
    public PresignedUpload requestUpload(UUID userId, String username, String contentType) {
        requireAllowedType(contentType);
        UUID profileId = profileService.getOrProvision(userId, username).getId();
        String extension = contentType.substring(contentType.indexOf('/') + 1);
        String key = "avatars/%s/%s.%s".formatted(profileId, UUID.randomUUID(), extension);
        return avatarStorage.presignUpload(key, contentType);
    }

    @Override
    @Transactional
    public Profile confirm(UUID userId, String key) {
        Profile profile = profileService.getByUserId(userId);
        if (!key.startsWith("avatars/" + profile.getId() + "/")) {
            throw new InvalidAvatarException("The avatar key does not belong to your profile.");
        }
        StoredObject object = avatarStorage
                .head(key)
                .orElseThrow(() -> new InvalidAvatarException("No uploaded file was found for that key."));
        if (object.contentLength() > maxSizeBytes) {
            throw new InvalidAvatarException("The image exceeds the maximum allowed size.");
        }
        requireAllowedType(object.contentType());

        String previousKey = profile.getAvatarKey();
        Profile saved = profileService.setAvatar(userId, key);
        if (previousKey != null && !previousKey.equals(key)) {
            avatarStorage.delete(previousKey);
        }
        log.info("Set avatar {} on profile {}", key, profile.getId());
        return saved;
    }

    @Override
    @Transactional
    public Profile remove(UUID userId) {
        Profile profile = profileService.getByUserId(userId);
        String key = profile.getAvatarKey();
        if (key == null) {
            return profile;
        }
        Profile saved = profileService.setAvatar(userId, null);
        avatarStorage.delete(key);
        return saved;
    }

    private void requireAllowedType(String contentType) {
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new InvalidAvatarException("Unsupported image type: " + contentType);
        }
    }
}
