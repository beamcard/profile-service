package com.beamcard.profile.rest.model.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.DayOfWeek;

public record OpeningHoursRequest(
        @NotNull DayOfWeek day,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "invalid_time") String open,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "invalid_time") String close) {

    @AssertTrue(message = "invalid_time_range") public boolean isRangeValid() {
        if (open == null || close == null) {
            return true;
        }
        return close.compareTo(open) > 0;
    }
}
