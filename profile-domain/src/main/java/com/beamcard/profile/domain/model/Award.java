package com.beamcard.profile.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Award {
    UUID id;
    UUID profileId;
    String imageKey;
    String description;
    int position;
    Instant createdAt;
}
