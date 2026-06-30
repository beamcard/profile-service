package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.time.Instant;

public record AvatarUploadResponse(String uploadUrl, String key, Instant expiresAt) {

    public static AvatarUploadResponse of(PresignedUpload upload) {
        return new AvatarUploadResponse(upload.url(), upload.key(), upload.expiresAt());
    }
}
