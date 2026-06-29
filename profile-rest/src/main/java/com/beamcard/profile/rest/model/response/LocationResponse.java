package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Location;

public record LocationResponse(String country, String city) {

    public static LocationResponse of(Location location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        return new LocationResponse(location.country(), location.city());
    }
}
