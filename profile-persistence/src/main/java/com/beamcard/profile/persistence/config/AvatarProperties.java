package com.beamcard.profile.persistence.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "beamcard.avatar")
public record AvatarProperties(
        String bucket,
        String region,
        String endpoint,
        String accessKey,
        String secretKey,
        boolean pathStyle,
        String publicBaseUrl,
        long presignTtlSeconds,
        long maxSizeBytes,
        List<String> allowedContentTypes) {

    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }

    public boolean hasEndpointOverride() {
        return endpoint != null && !endpoint.isBlank();
    }
}
