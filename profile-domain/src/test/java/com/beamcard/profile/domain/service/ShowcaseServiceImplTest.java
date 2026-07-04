package com.beamcard.profile.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.exception.InvalidShowcaseException;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.model.ShowcaseStep;
import com.beamcard.profile.domain.repository.ShowcaseRepository;
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
class ShowcaseServiceImplTest {

    private static final List<String> ALLOWED = List.of("image/png", "image/jpeg", "image/webp");

    @Mock
    ShowcaseRepository showcaseRepository;

    @Mock
    ProfileService profileService;

    @Mock
    MediaStorage mediaStorage;

    ShowcaseServiceImpl service;

    UUID userId;
    UUID profileId;
    String prefix;

    @BeforeEach
    void setUp() {
        service = new ShowcaseServiceImpl(showcaseRepository, profileService, mediaStorage, ALLOWED, 5_000_000L, 5);
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
        prefix = "showcases/" + profileId + "/";
    }

    private Profile profile() {
        return Profile.builder().id(profileId).userId(userId).username("alice").build();
    }

    @Test
    void requestUpload_presignsKeyUnderShowcases_withSubtypeExtension() {
        when(profileService.getOrProvision(userId, "alice")).thenReturn(profile());
        when(mediaStorage.presignUpload(any(), eq("image/png")))
                .thenReturn(new PresignedUpload("https://put", "k", Instant.now()));

        service.requestUpload(userId, "alice", "image/png");

        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(mediaStorage).presignUpload(key.capture(), eq("image/png"));
        assertThat(key.getValue()).startsWith(prefix).endsWith(".png");
    }

    @Test
    void requestUpload_rejectsUnsupportedType() {
        assertThatThrownBy(() -> service.requestUpload(userId, "alice", "application/pdf"))
                .isInstanceOf(InvalidShowcaseException.class);
        verify(mediaStorage, never()).presignUpload(any(), any());
    }

    @Test
    void replace_dropsImagelessStepsAndEmptyShowcases_andPersistsSanitized() {
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(showcaseRepository.findByProfileId(profileId)).thenReturn(List.of());
        when(mediaStorage.head(prefix + "before.png")).thenReturn(Optional.of(new StoredObject(100, "image/png")));

        List<Showcase> incoming = List.of(
                new Showcase(
                        "Braces",
                        "Crowded teeth",
                        List.of(
                                new ShowcaseStep(prefix + "before.png", "Before"),
                                new ShowcaseStep("  ", "no image — dropped"))),
                new Showcase("Empty", "no steps", List.of()));

        service.replace(userId, incoming);

        ArgumentCaptor<List<Showcase>> saved = ArgumentCaptor.captor();
        verify(showcaseRepository).replace(eq(profileId), saved.capture());
        assertThat(saved.getValue()).hasSize(1); // empty showcase dropped
        assertThat(saved.getValue().getFirst().steps()).hasSize(1); // imageless step dropped
        assertThat(saved.getValue().getFirst().steps().getFirst().imageKey()).isEqualTo(prefix + "before.png");
    }

    @Test
    void replace_rejectsImageKeyOfAnotherProfile() {
        when(profileService.getByUserId(userId)).thenReturn(profile());

        List<Showcase> incoming = List.of(
                new Showcase("X", null, List.of(new ShowcaseStep("showcases/" + UUID.randomUUID() + "/x.png", "hax"))));

        assertThatThrownBy(() -> service.replace(userId, incoming)).isInstanceOf(InvalidShowcaseException.class);
        verify(showcaseRepository, never()).replace(any(), any());
    }

    @Test
    void replace_rejectsMoreThanFiveSteps() {
        when(profileService.getByUserId(userId)).thenReturn(profile());

        List<ShowcaseStep> sixSteps = java.util.stream.IntStream.rangeClosed(1, 6)
                .mapToObj(n -> new ShowcaseStep(prefix + n + ".png", "step " + n))
                .toList();
        List<Showcase> incoming = List.of(new Showcase("Too long", null, sixSteps));

        assertThatThrownBy(() -> service.replace(userId, incoming)).isInstanceOf(InvalidShowcaseException.class);
        verify(showcaseRepository, never()).replace(any(), any());
    }

    @Test
    void replace_rejectsOversizedNewImage() {
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(showcaseRepository.findByProfileId(profileId)).thenReturn(List.of());
        when(mediaStorage.head(prefix + "big.png")).thenReturn(Optional.of(new StoredObject(5_000_001L, "image/png")));

        List<Showcase> incoming =
                List.of(new Showcase("X", null, List.of(new ShowcaseStep(prefix + "big.png", "huge"))));

        assertThatThrownBy(() -> service.replace(userId, incoming)).isInstanceOf(InvalidShowcaseException.class);
        verify(showcaseRepository, never()).replace(any(), any());
    }

    @Test
    void replace_deletesOrphanedImages() {
        when(profileService.getByUserId(userId)).thenReturn(profile());
        when(showcaseRepository.findByProfileId(profileId))
                .thenReturn(List.of(new Showcase(
                        "Old",
                        null,
                        List.of(
                                new ShowcaseStep(prefix + "keep.png", "a"),
                                new ShowcaseStep(prefix + "gone.png", "b")))));

        List<Showcase> incoming =
                List.of(new Showcase("New", null, List.of(new ShowcaseStep(prefix + "keep.png", "still here"))));

        service.replace(userId, incoming);

        verify(mediaStorage).delete(prefix + "gone.png");
        verify(mediaStorage, never()).delete(prefix + "keep.png");
    }
}
