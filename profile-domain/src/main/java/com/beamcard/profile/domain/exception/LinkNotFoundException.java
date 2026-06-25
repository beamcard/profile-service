package com.beamcard.profile.domain.exception;

import java.util.UUID;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(UUID id) {
        super("No link with id: " + id);
    }
}
