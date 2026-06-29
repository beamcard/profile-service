package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;

public record LocationResponse(String country, String city, String address) {

    public static LocationResponse of(Profile profile) {
        Location location = profile.getLocation();
        if (location == null || location.isEmpty()) {
            return null;
        }
        return new LocationResponse(location.country(), location.city(), location.address());
    }
}
