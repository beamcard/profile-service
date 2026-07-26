package com.beamcard.profile.domain.service;

import java.util.UUID;

public interface ProfileDeletionService {

    void deleteByUserId(UUID userId);
}
