package com.beamcard.profile.domain.storage;

import java.time.Instant;
import java.util.Optional;

/**
 * Object storage for profile media
 */
public interface MediaStorage {

    PresignedUpload presignUpload(String key, String contentType);

    Optional<StoredObject> head(String key);

    void delete(String key);

    String publicUrl(String key);

    record PresignedUpload(String url, String key, Instant expiresAt) {}

    record StoredObject(long contentLength, String contentType) {}
}
