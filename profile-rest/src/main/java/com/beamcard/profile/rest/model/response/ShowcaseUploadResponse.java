package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.time.Instant;

public record ShowcaseUploadResponse(String uploadUrl, String key, Instant expiresAt) {

    public static ShowcaseUploadResponse of(PresignedUpload upload) {
        return new ShowcaseUploadResponse(upload.url(), upload.key(), upload.expiresAt());
    }
}
