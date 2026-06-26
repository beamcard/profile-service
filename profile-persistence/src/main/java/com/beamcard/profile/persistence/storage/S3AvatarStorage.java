package com.beamcard.profile.persistence.storage;

import com.beamcard.profile.domain.storage.AvatarStorage;
import com.beamcard.profile.persistence.config.AvatarProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
@Slf4j
public class S3AvatarStorage implements AvatarStorage {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final AvatarProperties properties;

    @Override
    public PresignedUpload presignUpload(String key, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build();

        Duration ttl = Duration.ofSeconds(properties.presignTtlSeconds());
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .putObjectRequest(objectRequest)
                .build());

        return new PresignedUpload(
                presigned.url().toString(), key, Instant.now().plus(ttl));
    }

    @Override
    public Optional<StoredObject> head(String key) {
        try {
            HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build());
            return Optional.of(new StoredObject(response.contentLength(), response.contentType()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        } catch (S3Exception ex) {
            if (ex.statusCode() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(b -> b.bucket(properties.bucket()).key(key));
        log.debug("Deleted avatar object {}", key);
    }

    @Override
    public String publicUrl(String key) {
        return properties.publicBaseUrl() + "/" + key;
    }
}
