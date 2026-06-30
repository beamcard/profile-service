package com.beamcard.profile.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.exception.AwardNotFoundException;
import com.beamcard.profile.domain.exception.InvalidAwardException;
import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import com.beamcard.profile.domain.storage.MediaStorage.StoredObject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AwardServiceImplTest {

    private static final long MAX_BYTES = 100;
    private static final List<String> ALLOWED = List.of("image/png", "image/jpeg", "image/webp");

    @Mock
    AwardRepository awardRepository;

    @Mock
    ProfileService profileService;

    @Mock
    MediaStorage mediaStorage;

    AwardServiceImpl service;

    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        service = new AwardServiceImpl(awardRepository, profileService, mediaStorage, MAX_BYTES, ALLOWED);
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
    }

    private Profile profile() {
        return Profile.builder().id(profileId).userId(userId).username("alice").build();
    }

    private Award award(UUID id, int position) {
        return Award.builder()
                .id(id)
                .profileId(profileId)
                .imageKey("awards/" + profileId + "/" + id + ".png")
                .position(position)
                .build();
    }

    @Test
    void requestUpload_presignsKeyUnderProfileAwards_withSubtypeExtension() {
        when(profileService.getOrProvision(userId, "alice")).thenReturn(profile());
        when(mediaStorage.presignUpload(any(), eq("image/png")))
                .thenReturn(new PresignedUpload("https://put", "k", Instant.now()));

        service.requestUpload(userId, "alice", "image/png");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(mediaStorage).presignUpload(keyCaptor.capture(), eq("image/png"));
        assertThat(keyCaptor.getValue()).startsWith("awards/" + profileId + "/").endsWith(".png");
    }

    @Test
    void requestUpload_rejectsUnsupportedType() {
        assertThatThrownBy(() -> service.requestUpload(userId, "alice", "application/pdf"))
                .isInstanceOf(InvalidAwardException.class);
        verify(mediaStorage, never()).presignUpload(any(), any());
    }

    @Test
    void create_persistsAwardAtNextPosition() {
        String key = "awards/" + profileId + "/new.png";
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(mediaStorage.head(key)).thenReturn(Optional.of(new StoredObject(50, "image/png")));
        when(awardRepository.findByProfileId(profileId)).thenReturn(List.of(award(UUID.randomUUID(), 1)));
        when(awardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.create(userId, key);

        ArgumentCaptor<Award> saved = ArgumentCaptor.forClass(Award.class);
        verify(awardRepository).save(saved.capture());
        assertThat(saved.getValue().getImageKey()).isEqualTo(key);
        assertThat(saved.getValue().getProfileId()).isEqualTo(profileId);
        assertThat(saved.getValue().getPosition()).isEqualTo(2);
    }

    @Test
    void create_rejectsKeyNotOwnedByProfile() {
        when(profileService.getByUserId(userId)).thenReturn(profile());

        assertThatThrownBy(() -> service.create(userId, "awards/" + UUID.randomUUID() + "/x.png"))
                .isInstanceOf(InvalidAwardException.class);
        verify(awardRepository, never()).save(any());
    }

    @Test
    void create_rejectsWhenObjectMissing() {
        String key = "awards/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(mediaStorage.head(key)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(userId, key)).isInstanceOf(InvalidAwardException.class);
        verify(awardRepository, never()).save(any());
    }

    @Test
    void create_rejectsOversizedObject() {
        String key = "awards/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(mediaStorage.head(key)).thenReturn(Optional.of(new StoredObject(MAX_BYTES + 1, "image/png")));

        assertThatThrownBy(() -> service.create(userId, key)).isInstanceOf(InvalidAwardException.class);
        verify(awardRepository, never()).save(any());
    }

    @Test
    void create_rejectsDisallowedStoredContentType() {
        String key = "awards/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(mediaStorage.head(key)).thenReturn(Optional.of(new StoredObject(10, "application/pdf")));

        assertThatThrownBy(() -> service.create(userId, key)).isInstanceOf(InvalidAwardException.class);
        verify(awardRepository, never()).save(any());
    }

    @Test
    void update_setsDescriptionOnOwnedAward() {
        UUID awardId = UUID.randomUUID();
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(award(awardId, 1)));
        when(awardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AwardService.AwardView result = service.update(userId, awardId, "Board Certification 2024");

        assertThat(result.award().getDescription()).isEqualTo("Board Certification 2024");
    }

    @Test
    void update_blankDescriptionStoresNull() {
        UUID awardId = UUID.randomUUID();
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(award(awardId, 1)));
        when(awardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AwardService.AwardView result = service.update(userId, awardId, "   ");

        assertThat(result.award().getDescription()).isNull();
    }

    @Test
    void update_rejectsAwardOfAnotherProfile() {
        UUID awardId = UUID.randomUUID();
        Award foreign = Award.builder()
                .id(awardId)
                .profileId(UUID.randomUUID())
                .imageKey("awards/x/y.png")
                .position(1)
                .build();
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.update(userId, awardId, "x")).isInstanceOf(AwardNotFoundException.class);
        verify(awardRepository, never()).save(any());
    }

    @Test
    void delete_removesRowAndStorageObject_thenRecompacts() {
        UUID awardId = UUID.randomUUID();
        Award target = award(awardId, 1);
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(target));
        when(awardRepository.findByProfileId(profileId)).thenReturn(List.of());

        service.delete(userId, awardId);

        verify(awardRepository).deleteById(awardId);
        verify(mediaStorage).delete(target.getImageKey());
    }

    @Test
    void delete_rejectsAwardOfAnotherProfile() {
        UUID awardId = UUID.randomUUID();
        Award foreign = Award.builder()
                .id(awardId)
                .profileId(UUID.randomUUID())
                .imageKey("awards/x/y.png")
                .position(1)
                .build();
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findById(awardId)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.delete(userId, awardId)).isInstanceOf(AwardNotFoundException.class);
        verify(awardRepository, never()).deleteById(any());
        verify(mediaStorage, never()).delete(any());
    }

    @Test
    void reorder_assignsPositionsByGivenOrder() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findByProfileId(profileId)).thenReturn(List.of(award(a, 1), award(b, 2)));
        when(awardRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<AwardService.AwardView> result = service.reorder(userId, List.of(b, a));

        assertThat(result).extracting(v -> v.award().getId()).containsExactly(b, a);
        assertThat(result).extracting(v -> v.award().getPosition()).containsExactly(1, 2);
    }

    @Test
    void reorder_rejectsUnknownId() {
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(awardRepository.findByProfileId(profileId)).thenReturn(List.of(award(UUID.randomUUID(), 1)));

        assertThatThrownBy(() -> service.reorder(userId, List.of(UUID.randomUUID())))
                .isInstanceOf(AwardNotFoundException.class);
        verify(awardRepository, never()).saveAll(any());
    }
}
