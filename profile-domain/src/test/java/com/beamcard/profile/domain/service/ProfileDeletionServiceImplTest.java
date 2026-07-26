package com.beamcard.profile.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.model.ShowcaseStep;
import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.domain.repository.ShowcaseRepository;
import com.beamcard.profile.domain.storage.MediaStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileDeletionServiceImplTest {

    @Mock
    ProfileRepository profileRepository;

    @Mock
    AwardRepository awardRepository;

    @Mock
    ShowcaseRepository showcaseRepository;

    @Mock
    MediaStorage mediaStorage;

    @InjectMocks
    ProfileDeletionServiceImpl service;

    UUID userId;
    UUID profileId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
    }

    @Test
    void delete_removesAllMedia_thenDeletesProfileRow() {
        Profile profile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .avatarKey("avatars/p/a.png")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(awardRepository.findByProfileId(profileId))
                .thenReturn(List.of(
                        Award.builder().imageKey("awards/p/1.png").build(),
                        Award.builder().imageKey("awards/p/2.png").build()));
        when(showcaseRepository.findByProfileId(profileId))
                .thenReturn(List.of(new Showcase(
                        "Case",
                        null,
                        List.of(
                                new ShowcaseStep("showcases/p/s1.png", "before"),
                                new ShowcaseStep(null, "no image")))));

        service.deleteByUserId(userId);

        verify(mediaStorage).delete("avatars/p/a.png");
        verify(mediaStorage).delete("awards/p/1.png");
        verify(mediaStorage).delete("awards/p/2.png");
        verify(mediaStorage).delete("showcases/p/s1.png");
        verify(mediaStorage, never()).delete(null);
        verify(profileRepository).deleteByUserId(userId);
    }

    @Test
    void delete_isNoOp_whenProfileAbsent() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        service.deleteByUserId(userId);

        verify(mediaStorage, never()).delete(any());
        verify(profileRepository, never()).deleteByUserId(any());
    }

    @Test
    void delete_continues_whenAMediaDeleteFails() {
        Profile profile = Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .avatarKey("avatars/p/a.png")
                .build();
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(awardRepository.findByProfileId(profileId)).thenReturn(List.of());
        when(showcaseRepository.findByProfileId(profileId)).thenReturn(List.of());
        org.mockito.Mockito.doThrow(new RuntimeException("S3 down"))
                .when(mediaStorage)
                .delete("avatars/p/a.png");

        service.deleteByUserId(userId);

        verify(profileRepository).deleteByUserId(userId);
    }
}
