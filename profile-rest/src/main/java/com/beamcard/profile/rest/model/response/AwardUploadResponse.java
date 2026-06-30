package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.time.Instant;

public record AwardUploadResponse(String uploadUrl, String key, Instant expiresAt) {

    public static AwardUploadResponse of(PresignedUpload upload) {
        return new AwardUploadResponse(upload.url(), upload.key(), upload.expiresAt());
    }
}
