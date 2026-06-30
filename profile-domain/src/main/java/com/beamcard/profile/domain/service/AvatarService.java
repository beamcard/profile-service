package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.util.UUID;

public interface AvatarService {

    PresignedUpload requestUpload(UUID userId, String username, String contentType);

    Profile confirm(UUID userId, String key);

    Profile remove(UUID userId);
}
