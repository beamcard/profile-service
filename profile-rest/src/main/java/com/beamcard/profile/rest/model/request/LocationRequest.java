package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.Size;

public record LocationRequest(
        @Size(max = 60) String country, @Size(max = 85) String city, @Size(max = 200) String address) {}
