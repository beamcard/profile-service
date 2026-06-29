package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.model.AffiliationJpa;
import com.beamcard.profile.persistence.model.ProfileJpa;
import com.beamcard.profile.persistence.model.ProfileLocationJpa;
import com.beamcard.profile.persistence.repository.jpa.AffiliationJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileLocationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfileJpaRepository jpaRepository;
    private final ProfileLocationJpaRepository locationRepository;
    private final AffiliationJpaRepository affiliationRepository;
    private final ProfilePersistenceMapper mapper;

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(this::composeProfile);
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::composeProfile);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpa saved = jpaRepository.save(mapper.toJpa(profile));
        saveLocation(saved.getId(), profile.getLocation());
        if (profile.getAffiliations() != null) {
            replaceAffiliations(saved.getId(), profile.getAffiliations());
        }
        return composeProfile(saved);
    }

    private Profile composeProfile(ProfileJpa base) {
        Profile.ProfileBuilder profile = mapper.toDomain(base).toBuilder();
        locationRepository
                .findById(base.getId())
                .map(ProfileRepositoryImpl::toLocation)
                .ifPresent(profile::location);
        return profile.affiliations(loadAffiliations(base.getId())).build();
    }

    private List<Affiliation> loadAffiliations(UUID profileId) {
        return affiliationRepository.findByProfileIdOrderByPositionAsc(profileId).stream()
                .map(ProfileRepositoryImpl::toAffiliation)
                .toList();
    }

    private static Location toLocation(ProfileLocationJpa locationJpa) {
        return new Location(locationJpa.getCountry(), locationJpa.getCity());
    }

    private static Affiliation toAffiliation(AffiliationJpa affiliationJpa) {
        return new Affiliation(
                affiliationJpa.getRole(),
                affiliationJpa.getOrganization(),
                affiliationJpa.getAddress(),
                affiliationJpa.getDescription());
    }

    private void saveLocation(UUID profileId, Location location) {
        if (location == null) {
            return;
        }
        if (location.isEmpty()) {
            if (locationRepository.existsById(profileId)) {
                locationRepository.deleteById(profileId);
            }
            return;
        }
        locationRepository.save(ProfileLocationJpa.builder()
                .profileId(profileId)
                .country(blankToNull(location.country()))
                .city(blankToNull(location.city()))
                .build());
    }

    private void replaceAffiliations(UUID profileId, List<Affiliation> affiliations) {
        affiliationRepository.deleteByProfileId(profileId);
        int position = 0;
        for (Affiliation affiliation : affiliations) {
            if (affiliation == null || affiliation.isEmpty()) {
                continue;
            }
            affiliationRepository.save(AffiliationJpa.builder()
                    .profileId(profileId)
                    .role(blankToNull(affiliation.role()))
                    .organization(blankToNull(affiliation.organization()))
                    .address(blankToNull(affiliation.address()))
                    .description(blankToNull(affiliation.description()))
                    .position(position++)
                    .build());
        }
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
