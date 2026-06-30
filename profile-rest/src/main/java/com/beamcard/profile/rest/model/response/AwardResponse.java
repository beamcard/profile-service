package com.beamcard.profile.rest.model.response;

import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.service.AwardService.AwardView;
import java.util.List;
import java.util.UUID;

public record AwardResponse(UUID id, String imageUrl, String description, int position) {

    public static AwardResponse of(AwardView view) {
        Award award = view.award();
        return new AwardResponse(award.getId(), view.imageUrl(), award.getDescription(), award.getPosition());
    }

    public static List<AwardResponse> listOf(List<AwardView> views) {
        return views.stream().map(AwardResponse::of).toList();
    }
}
