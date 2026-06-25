package com.beamcard.profile.domain.exception;

import java.util.UUID;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String username) {
        super("No profile for username: " + username);
    }

    public ProfileNotFoundException(UUID userId) {
        super("No profile for user: " + userId);
    }
}
