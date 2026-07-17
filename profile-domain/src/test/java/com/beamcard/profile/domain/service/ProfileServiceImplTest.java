package com.beamcard.profile.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.exception.ProfileNotFoundException;
import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Currency;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.PriceItem;
import com.beamcard.profile.domain.model.PriceType;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.domain.service.ProfileService.UpdateProfileCommand;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    ProfileRepository profileRepository;

    @InjectMocks
    ProfileServiceImpl service;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getOrProvision_returnsExisting_withoutSaving() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("alice")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        assertThat(service.getOrProvision(userId, "alice")).isSameAs(existing);
        verify(profileRepository, never()).save(any());
    }

    @Test
    void getOrProvision_createsProfile_whenAbsent() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.getOrProvision(userId, "alice");

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
        verify(profileRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getUsername()).isEqualTo("alice");
        assertThat(captor.getValue().getId()).isNull(); // assigned by persistence
    }

    @Test
    void getOrProvision_reconcilesHandleAndLocale_whenTokenDiffers() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("old_handle")
                .locale("en")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Profile result = service.getOrProvision(userId, "new_handle", "de");

        assertThat(result.getUsername()).isEqualTo("new_handle");
        assertThat(result.getLocale()).isEqualTo("de");
    }

    @Test
    void getOrProvision_twoArg_reconcilesHandleButLeavesLocaleUntouched() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("old_handle")
                .locale("de")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Profile result = service.getOrProvision(userId, "new_handle");

        assertThat(result.getUsername()).isEqualTo("new_handle");
        assertThat(result.getLocale()).isEqualTo("de");
    }

    @Test
    void getByUsername_returnsProfile_whenFound() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("alice")
                .build();
        when(profileRepository.findByUsername("alice")).thenReturn(Optional.of(existing));

        assertThat(service.getByUsername("alice")).isSameAs(existing);
        verify(profileRepository, never()).save(any());
    }

    @Test
    void getByUsername_throws_whenAbsent() {
        when(profileRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByUsername("ghost")).isInstanceOf(ProfileNotFoundException.class);
    }

    @Test
    void update_appliesOnlyNonNullFields() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("alice")
                .displayName("Old")
                .bio("keep me")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Affiliation affiliation = new Affiliation("Guide", "Acme", "Stephansplatz 1", "Entrance B");
        Profile result = service.update(
                userId,
                "alice",
                new UpdateProfileCommand(
                        "New name",
                        null,
                        "+380671211111",
                        new Location("Austria", "Vienna"),
                        List.of(affiliation),
                        null,
                        null,
                        null));

        assertThat(result.getDisplayName()).isEqualTo("New name");
        assertThat(result.getBio()).isEqualTo("keep me");
        assertThat(result.getPhone()).isEqualTo("+380671211111");
        assertThat(result.getLocation().city()).isEqualTo("Vienna");
        assertThat(result.getLocation().country()).isEqualTo("Austria");
        assertThat(result.getAffiliations()).hasSize(1);
        assertThat(result.getAffiliations().getFirst().role()).isEqualTo("Guide");
        assertThat(result.getAffiliations().getFirst().organization()).isEqualTo("Acme");
        assertThat(result.getAffiliations().getFirst().address()).isEqualTo("Stephansplatz 1");
        assertThat(result.getAffiliations().getFirst().description()).isEqualTo("Entrance B");
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void update_appliesCurrencyAndPriceItems() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("alice")
                .currency(Currency.USD)
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<PriceItem> items = List.of(
                new PriceItem("Consultation", PriceType.EXACT, new BigDecimal("50.00"), null),
                new PriceItem("Full project", PriceType.RANGE, new BigDecimal("500"), new BigDecimal("1200")));
        Profile result = service.update(
                userId, "alice", new UpdateProfileCommand(null, null, null, null, null, null, Currency.EUR, items));

        assertThat(result.getCurrency()).isEqualTo(Currency.EUR);
        assertThat(result.getPriceItems()).hasSize(2);
        assertThat(result.getPriceItems().getFirst().name()).isEqualTo("Consultation");
        assertThat(result.getPriceItems().getFirst().priceType()).isEqualTo(PriceType.EXACT);
        assertThat(result.getPriceItems().getLast().amountMax()).isEqualByComparingTo("1200");
    }

    @Test
    void update_leavesCurrencyUnchanged_whenNull() {
        Profile existing = Profile.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .username("alice")
                .currency(Currency.UAH)
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Profile result = service.update(
                userId, "alice", new UpdateProfileCommand("New name", null, null, null, null, null, null, null));

        assertThat(result.getCurrency()).isEqualTo(Currency.UAH);
    }
}
