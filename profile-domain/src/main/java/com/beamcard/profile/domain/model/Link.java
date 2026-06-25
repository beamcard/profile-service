package com.beamcard.profile.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Link {
    UUID id;
    UUID profileId;
    String label;
    String url;
    LinkType type;
    int position;
    Instant createdAt;
}
