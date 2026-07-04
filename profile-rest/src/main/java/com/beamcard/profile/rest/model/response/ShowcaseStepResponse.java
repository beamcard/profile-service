package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.service.ShowcaseService.StepView;

public record ShowcaseStepResponse(String imageKey, String imageUrl, String description) {

    public static ShowcaseStepResponse of(StepView step) {
        return new ShowcaseStepResponse(step.imageKey(), step.imageUrl(), step.description());
    }
}
