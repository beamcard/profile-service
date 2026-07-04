package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.service.ShowcaseService.ShowcaseView;
import java.util.List;

public record ShowcaseResponse(String title, String intro, List<ShowcaseStepResponse> steps) {

    public static ShowcaseResponse of(ShowcaseView view) {
        return new ShowcaseResponse(
                view.title(),
                view.intro(),
                view.steps().stream().map(ShowcaseStepResponse::of).toList());
    }

    public static List<ShowcaseResponse> listOf(List<ShowcaseView> views) {
        return views.stream().map(ShowcaseResponse::of).toList();
    }
}
