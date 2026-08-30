package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

import com.beamcard.profile.domain.exception.InvalidHoursException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record OpeningHours(DayOfWeek day, String open, String close) {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("H:mm");

    public boolean isEmpty() {
        return day == null || !hasText(open) || !hasText(close);
    }

    public void validate() {
        if (day == null) {
            throw new InvalidHoursException("Opening hours must specify a weekday.");
        }
        if (!openTime().isBefore(closeTime())) {
            throw new InvalidHoursException("Closing time must be after opening time.");
        }
    }

    public boolean overlaps(OpeningHours other) {
        return day == other.day
                && openTime().isBefore(other.closeTime())
                && other.openTime().isBefore(closeTime());
    }

    private LocalTime openTime() {
        return parse(open, "Opening");
    }

    private LocalTime closeTime() {
        return parse(close, "Closing");
    }

    private static LocalTime parse(String value, String which) {
        try {
            return LocalTime.parse(value.trim(), HH_MM);
        } catch (RuntimeException e) {
            throw new InvalidHoursException(which + " time must be a valid HH:MM value.");
        }
    }
}
