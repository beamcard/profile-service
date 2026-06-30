package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import java.util.List;
import java.util.UUID;

public interface AwardService {

    List<AwardView> listForDisplay(UUID profileId);

    PresignedUpload requestUpload(UUID userId, String username, String contentType);

    AwardView create(UUID userId, String key);

    AwardView update(UUID userId, UUID awardId, String description);

    void delete(UUID userId, UUID awardId);

    List<AwardView> reorder(UUID userId, List<UUID> orderedIds);

    record AwardView(Award award, String imageUrl) {}
}
