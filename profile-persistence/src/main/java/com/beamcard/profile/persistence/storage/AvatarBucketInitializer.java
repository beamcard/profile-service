package com.beamcard.profile.persistence.storage;

import com.beamcard.profile.persistence.config.AvatarProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@RequiredArgsConstructor
@Slf4j
public class AvatarBucketInitializer implements ApplicationRunner {

    private final S3Client s3Client;
    private final AvatarProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        String bucket = properties.bucket();
        try {
            s3Client.headBucket(b -> b.bucket(bucket));
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(b -> b.bucket(bucket));
            log.info("Created avatar bucket '{}'", bucket);
        }
        // LOCAL ONLY: gated by beamcard.avatar.ensure-bucket=true (MinIO dev).
        String policy =
                """
                {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":"*",\
                "Action":"s3:GetObject","Resource":"arn:aws:s3:::%s/*"}]}"""
                        .formatted(bucket);
        s3Client.putBucketPolicy(b -> b.bucket(bucket).policy(policy));
        log.info("Ensured public-read policy on avatar bucket '{}'", bucket);
    }
}
