package com.beamcard.profile.domain.model;

import java.time.Instant;
import java.util.List;
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
    String phone;
    Location location;
    List<Affiliation> affiliations;
    List<String> activities;
    Currency currency;
    List<PriceItem> priceItems;
    String avatarKey;
    String locale;
    Instant createdAt;
    Instant updatedAt;
}
