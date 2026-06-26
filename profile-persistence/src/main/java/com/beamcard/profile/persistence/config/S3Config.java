package com.beamcard.profile.persistence.config;

import com.beamcard.profile.domain.storage.AvatarStorage;
import com.beamcard.profile.persistence.storage.AvatarBucketInitializer;
import com.beamcard.profile.persistence.storage.S3AvatarStorage;
import java.net.URI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(AvatarProperties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(AvatarProperties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .forcePathStyle(properties.pathStyle())
                .credentialsProvider(credentials(properties));
        if (properties.hasEndpointOverride()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AvatarProperties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.pathStyle())
                        .build())
                .credentialsProvider(credentials(properties));
        if (properties.hasEndpointOverride()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    @Bean
    public AvatarStorage avatarStorage(S3Client s3Client, S3Presigner s3Presigner, AvatarProperties properties) {
        return new S3AvatarStorage(s3Client, s3Presigner, properties);
    }

    @Bean
    @ConditionalOnProperty(name = "beamcard.avatar.ensure-bucket", havingValue = "true")
    public AvatarBucketInitializer avatarBucketInitializer(S3Client s3Client, AvatarProperties properties) {
        return new AvatarBucketInitializer(s3Client, properties);
    }

    private static AwsCredentialsProvider credentials(AvatarProperties properties) {
        if (properties.hasStaticCredentials()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
        }
        return DefaultCredentialsProvider.create();
    }
}
