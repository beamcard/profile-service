package com.beamcard.profile.rest.utils;

import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

public final class JwtClaimsUtil {

    private JwtClaimsUtil() {}

    public static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    public static String username(Jwt jwt) {
        return jwt.getClaimAsString("username");
    }
}
