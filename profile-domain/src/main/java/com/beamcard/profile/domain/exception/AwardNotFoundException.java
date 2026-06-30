package com.beamcard.profile.domain.exception;

import java.util.UUID;

public class AwardNotFoundException extends RuntimeException {
    public AwardNotFoundException(UUID id) {
        super("No award with id: " + id);
    }
}
