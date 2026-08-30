package com.beamcard.profile.domain.exception;

public class OverlappingHoursException extends RuntimeException {
    public OverlappingHoursException(String message) {
        super(message);
    }
}
