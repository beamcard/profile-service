package com.beamcard.profile.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.model.AffiliationJpa;
import com.beamcard.profile.persistence.model.ProfileJpa;
import com.beamcard.profile.persistence.model.ProfileLocationJpa;
import com.beamcard.profile.persistence.repository.jpa.ActivityJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.AffiliationJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.PriceItemJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileLocationJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileRepositoryImplTest {

    @Mock
    ProfileJpaRepository jpaRepository;

    @Mock
    ProfileLocationJpaRepository locationRepository;

    @Mock
    AffiliationJpaRepository affiliationRepository;

    @Mock
    ActivityJpaRepository activityRepository;

    @Mock
    PriceItemJpaRepository priceItemRepository;

    final ProfilePersistenceMapper mapper = Mappers.getMapper(ProfilePersistenceMapper.class);

    ProfileRepositoryImpl repository;

    UUID profileId;
    UUID userId;

    @BeforeEach
    void setUp() {
        repository = new ProfileRepositoryImpl(
                jpaRepository,
                locationRepository,
                affiliationRepository,
                activityRepository,
                priceItemRepository,
                mapper);
        profileId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    private ProfileJpa profileJpa() {
        return ProfileJpa.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .build();
    }

    @Test
    void findByUserId_composesPrimaryLocationAndOrderedAffiliations() {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.of(profileJpa()));
        when(locationRepository.findById(profileId))
                .thenReturn(Optional.of(ProfileLocationJpa.builder()
                        .profileId(profileId)
                        .country("Austria")
                        .city("Vienna")
                        .build()));
        when(affiliationRepository.findByProfileIdOrderByPositionAsc(profileId))
                .thenReturn(List.of(
                        AffiliationJpa.builder()
                                .profileId(profileId)
                                .role("Trainer")
                                .organization("FitGym")
                                .address("Stephansplatz 1")
                                .position(0)
                                .build(),
                        AffiliationJpa.builder()
                                .profileId(profileId)
                                .role("Trainer")
                                .organization("PowerHouse")
                                .position(1)
                                .build()));

        Profile result = repository.findByUserId(userId).orElseThrow();

        assertThat(result.getLocation().country()).isEqualTo("Austria");
        assertThat(result.getLocation().city()).isEqualTo("Vienna");
        assertThat(result.getAffiliations()).hasSize(2);
        assertThat(result.getAffiliations().get(0).organization()).isEqualTo("FitGym");
        assertThat(result.getAffiliations().get(0).address()).isEqualTo("Stephansplatz 1");
        assertThat(result.getAffiliations().get(1).address()).isNull();
    }

    @Test
    void findByUserId_noLocationRow_leavesLocationNull() {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.of(profileJpa()));
        when(locationRepository.findById(profileId)).thenReturn(Optional.empty());
        when(affiliationRepository.findByProfileIdOrderByPositionAsc(profileId)).thenReturn(List.of());

        Profile result = repository.findByUserId(userId).orElseThrow();

        assertThat(result.getLocation()).isNull();
        assertThat(result.getAffiliations()).isEmpty();
    }

    @Test
    void save_upsertsPrimaryLocation_andReplacesAffiliations_skippingBlank() {
        Profile domain = Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .location(new Location("Austria", "Vienna"))
                .affiliations(List.of(
                        new Affiliation("Trainer", "FitGym", "Stephansplatz 1", "Entrance B"),
                        new Affiliation("  ", null, "  ", null), // blank → skipped
                        new Affiliation(null, "PowerHouse", null, null)))
                .build();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(locationRepository.findById(profileId)).thenReturn(Optional.empty());
        when(affiliationRepository.findByProfileIdOrderByPositionAsc(profileId)).thenReturn(List.of());

        repository.save(domain);

        ArgumentCaptor<ProfileLocationJpa> loc = ArgumentCaptor.forClass(ProfileLocationJpa.class);
        verify(locationRepository).save(loc.capture());
        assertThat(loc.getValue().getCity()).isEqualTo("Vienna");

        verify(affiliationRepository).deleteByProfileId(profileId);
        ArgumentCaptor<AffiliationJpa> aff = ArgumentCaptor.forClass(AffiliationJpa.class);
        verify(affiliationRepository, org.mockito.Mockito.times(2)).save(aff.capture());
        assertThat(aff.getAllValues().get(0).getOrganization()).isEqualTo("FitGym");
        assertThat(aff.getAllValues().get(0).getDescription()).isEqualTo("Entrance B");
        assertThat(aff.getAllValues().get(0).getPosition()).isEqualTo(0);
        assertThat(aff.getAllValues().get(1).getOrganization()).isEqualTo("PowerHouse");
        assertThat(aff.getAllValues().get(1).getPosition()).isEqualTo(1);
    }
}
