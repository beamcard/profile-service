package com.beamcard.profile.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.model.ProfileJpa;
import com.beamcard.profile.persistence.model.ProfileLocationJpa;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileLocationJpaRepository;
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

    final ProfilePersistenceMapper mapper = Mappers.getMapper(ProfilePersistenceMapper.class);

    ProfileRepositoryImpl repository;

    UUID profileId;
    UUID userId;

    @BeforeEach
    void setUp() {
        repository = new ProfileRepositoryImpl(jpaRepository, locationRepository, mapper);
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
    void findByUserId_mergesLocationFromChildTable() {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.of(profileJpa()));
        when(locationRepository.findById(profileId))
                .thenReturn(Optional.of(ProfileLocationJpa.builder()
                        .profileId(profileId)
                        .country("Austria")
                        .city("Vienna")
                        .address("Stephansplatz 1")
                        .build()));

        Profile result = repository.findByUserId(userId).orElseThrow();

        assertThat(result.getLocation().country()).isEqualTo("Austria");
        assertThat(result.getLocation().city()).isEqualTo("Vienna");
        assertThat(result.getLocation().address()).isEqualTo("Stephansplatz 1");
    }

    @Test
    void findByUserId_noLocationRow_leavesLocationNull() {
        when(jpaRepository.findByUserId(userId)).thenReturn(Optional.of(profileJpa()));
        when(locationRepository.findById(profileId)).thenReturn(Optional.empty());

        Profile result = repository.findByUserId(userId).orElseThrow();

        assertThat(result.getLocation()).isNull();
    }

    @Test
    void save_upsertsLocationRow_whenLocationPresent() {
        Profile domain = Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .location(new Location("Austria", "Vienna", null))
                .build();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        repository.save(domain);

        ArgumentCaptor<ProfileLocationJpa> captor = ArgumentCaptor.forClass(ProfileLocationJpa.class);
        verify(locationRepository).save(captor.capture());
        assertThat(captor.getValue().getProfileId()).isEqualTo(profileId);
        assertThat(captor.getValue().getCountry()).isEqualTo("Austria");
        assertThat(captor.getValue().getCity()).isEqualTo("Vienna");
        assertThat(captor.getValue().getAddress()).isNull();
        verify(locationRepository, never()).deleteById(any());
    }

    @Test
    void save_deletesLocationRow_whenAllFieldsBlank() {
        Profile domain = Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .location(new Location("  ", null, null))
                .build();
        when(jpaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(locationRepository.existsById(profileId)).thenReturn(true);

        repository.save(domain);

        verify(locationRepository).deleteById(profileId);
        verify(locationRepository, never()).save(any());
    }
}
