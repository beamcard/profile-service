package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.OpeningHours;
import com.beamcard.profile.domain.model.PriceItem;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.model.ActivityJpa;
import com.beamcard.profile.persistence.model.AffiliationJpa;
import com.beamcard.profile.persistence.model.OpeningHoursJpa;
import com.beamcard.profile.persistence.model.PriceItemJpa;
import com.beamcard.profile.persistence.model.ProfileJpa;
import com.beamcard.profile.persistence.model.ProfileLocationJpa;
import com.beamcard.profile.persistence.repository.jpa.ActivityJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.AffiliationJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.OpeningHoursJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.PriceItemJpaRepository;
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
    private final ActivityJpaRepository activityRepository;
    private final PriceItemJpaRepository priceItemRepository;
    private final OpeningHoursJpaRepository openingHoursRepository;
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
    public void deleteByUserId(UUID userId) {
        jpaRepository.deleteByUserId(userId);
    }

    @Override
    public Profile save(Profile profile) {
        ProfileJpa saved = jpaRepository.save(mapper.toJpa(profile));
        saveLocation(saved.getId(), profile.getLocation());
        if (profile.getAffiliations() != null) {
            replaceAffiliations(saved.getId(), profile.getAffiliations());
        }
        if (profile.getActivities() != null) {
            replaceActivities(saved.getId(), profile.getActivities());
        }
        if (profile.getPriceItems() != null) {
            replacePriceItems(saved.getId(), profile.getPriceItems());
        }
        return composeProfile(saved);
    }

    private Profile composeProfile(ProfileJpa base) {
        Profile.ProfileBuilder profile = mapper.toDomain(base).toBuilder();
        locationRepository
                .findById(base.getId())
                .map(ProfileRepositoryImpl::toLocation)
                .ifPresent(profile::location);
        return profile.affiliations(loadAffiliations(base.getId()))
                .activities(loadActivities(base.getId()))
                .priceItems(loadPriceItems(base.getId()))
                .build();
    }

    private List<Affiliation> loadAffiliations(UUID profileId) {
        return affiliationRepository.findByProfileIdOrderByPositionAsc(profileId).stream()
                .map(this::toAffiliation)
                .toList();
    }

    private List<String> loadActivities(UUID profileId) {
        return activityRepository.findByProfileIdOrderByPositionAsc(profileId).stream()
                .map(ActivityJpa::getName)
                .toList();
    }

    private List<PriceItem> loadPriceItems(UUID profileId) {
        return priceItemRepository.findByProfileIdOrderByPositionAsc(profileId).stream()
                .map(ProfileRepositoryImpl::toPriceItem)
                .toList();
    }

    private List<OpeningHours> loadOpeningHours(UUID affiliationId) {
        return openingHoursRepository.findByAffiliationIdOrderByPositionAsc(affiliationId).stream()
                .map(ProfileRepositoryImpl::toOpeningHours)
                .toList();
    }

    private static Location toLocation(ProfileLocationJpa locationJpa) {
        return new Location(locationJpa.getCountry(), locationJpa.getCity());
    }

    private Affiliation toAffiliation(AffiliationJpa affiliationJpa) {
        return new Affiliation(
                affiliationJpa.getRole(),
                affiliationJpa.getOrganization(),
                affiliationJpa.getAddress(),
                affiliationJpa.getDescription(),
                loadOpeningHours(affiliationJpa.getId()));
    }

    private static PriceItem toPriceItem(PriceItemJpa priceItemJpa) {
        return new PriceItem(
                priceItemJpa.getName(),
                priceItemJpa.getPriceType(),
                priceItemJpa.getAmountMin(),
                priceItemJpa.getAmountMax(),
                priceItemJpa.getDurationMinutes());
    }

    private static OpeningHours toOpeningHours(OpeningHoursJpa jpa) {
        return new OpeningHours(jpa.getDay(), jpa.getOpenTime(), jpa.getCloseTime());
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
        affiliationRepository.deleteByProfileId(profileId); // cascade removes their opening_hours
        int position = 0;
        for (Affiliation affiliation : affiliations) {
            if (affiliation == null || affiliation.isEmpty()) {
                continue;
            }
            AffiliationJpa saved = affiliationRepository.save(AffiliationJpa.builder()
                    .profileId(profileId)
                    .role(blankToNull(affiliation.role()))
                    .organization(blankToNull(affiliation.organization()))
                    .address(blankToNull(affiliation.address()))
                    .description(blankToNull(affiliation.description()))
                    .position(position++)
                    .build());
            saveOpeningHours(saved.getId(), affiliation.openingHours());
        }
    }

    private void saveOpeningHours(UUID affiliationId, List<OpeningHours> openingHours) {
        if (openingHours == null) {
            return;
        }
        int position = 0;
        for (OpeningHours hours : openingHours) {
            if (hours == null || hours.isEmpty()) {
                continue;
            }
            openingHoursRepository.save(OpeningHoursJpa.builder()
                    .affiliationId(affiliationId)
                    .day(hours.day())
                    .openTime(hours.open().trim())
                    .closeTime(hours.close().trim())
                    .position(position++)
                    .build());
        }
    }

    private void replaceActivities(UUID profileId, List<String> activities) {
        activityRepository.deleteByProfileId(profileId);
        int position = 0;
        for (String activity : activities) {
            if (!StringUtils.hasText(activity)) {
                continue;
            }
            activityRepository.save(ActivityJpa.builder()
                    .profileId(profileId)
                    .name(activity.trim())
                    .position(position++)
                    .build());
        }
    }

    private void replacePriceItems(UUID profileId, List<PriceItem> priceItems) {
        priceItemRepository.deleteByProfileId(profileId);
        int position = 0;
        for (PriceItem item : priceItems) {
            if (item == null || item.isEmpty()) {
                continue;
            }
            priceItemRepository.save(PriceItemJpa.builder()
                    .profileId(profileId)
                    .name(item.name().trim())
                    .priceType(item.priceType())
                    .amountMin(item.amountMin())
                    .amountMax(item.amountMax())
                    .durationMinutes(item.durationMinutes())
                    .position(position++)
                    .build());
        }
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
