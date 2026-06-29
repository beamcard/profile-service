package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.model.ProfileJpa;
import com.beamcard.profile.persistence.model.ProfileLocationJpa;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileLocationJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfileJpaRepository jpaRepository;
    private final ProfileLocationJpaRepository locationRepository;
    private final ProfilePersistenceMapper mapper;

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(this::toDomainWithLocation);
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomainWithLocation);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpa saved = jpaRepository.save(mapper.toJpa(profile));
        saveLocation(saved.getId(), profile);
        return toDomainWithLocation(saved);
    }

    private Profile toDomainWithLocation(ProfileJpa jpa) {
        Profile profile = mapper.toDomain(jpa);
        return locationRepository
                .findById(jpa.getId())
                .map(loc -> profile.toBuilder()
                        .location(new Location(loc.getCountry(), loc.getCity(), loc.getAddress()))
                        .build())
                .orElse(profile);
    }

    private void saveLocation(UUID profileId, Profile profile) {
        Location location = profile.getLocation();
        if (location == null || location.isEmpty()) {
            if (locationRepository.existsById(profileId)) {
                locationRepository.deleteById(profileId);
            }
            return;
        }
        locationRepository.save(ProfileLocationJpa.builder()
                .profileId(profileId)
                .country(blankToNull(location.country()))
                .city(blankToNull(location.city()))
                .address(blankToNull(location.address()))
                .build());
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
