package com.beamcard.profile.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Profile {
    UUID id;
    UUID userId;
    String username;
    String displayName;
    String bio;
    Instant createdAt;
    Instant updatedAt;
}
