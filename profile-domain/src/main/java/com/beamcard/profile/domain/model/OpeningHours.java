package com.beamcard.profile.domain.model;

import static org.springframework.util.StringUtils.hasText;

import java.time.DayOfWeek;

public record OpeningHours(DayOfWeek day, String open, String close) {

    public boolean isEmpty() {
        return day == null || !hasText(open) || !hasText(close);
    }
}
