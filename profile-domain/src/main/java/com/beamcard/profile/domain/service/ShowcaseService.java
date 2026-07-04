package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.util.List;
import java.util.UUID;

public interface ShowcaseService {

    List<ShowcaseView> listForDisplay(UUID profileId);

    PresignedUpload requestUpload(UUID userId, String username, String contentType);

    List<ShowcaseView> replace(UUID userId, List<Showcase> showcases);

    record ShowcaseView(String title, String intro, List<StepView> steps) {}

    record StepView(String imageKey, String imageUrl, String description) {}
}
