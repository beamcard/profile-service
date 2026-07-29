package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.OpeningHours;
import java.time.DayOfWeek;

public record OpeningHoursResponse(DayOfWeek day, String open, String close) {

    public static OpeningHoursResponse of(OpeningHours hours) {
        return new OpeningHoursResponse(hours.day(), hours.open(), hours.close());
    }
}
