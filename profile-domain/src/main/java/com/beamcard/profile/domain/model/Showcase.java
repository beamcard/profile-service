package com.beamcard.profile.domain.model;

import java.util.List;

public record Showcase(String title, String intro, List<ShowcaseStep> steps) {

    public List<ShowcaseStep> stepsOrEmpty() {
        return steps == null ? List.of() : steps;
    }
}
