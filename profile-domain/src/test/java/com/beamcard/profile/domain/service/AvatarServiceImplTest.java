package com.beamcard.profile.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.beamcard.profile.domain.exception.InvalidAvatarException;
import com.beamcard.profile.domain.model.Profile;
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
class AvatarServiceImplTest {

    private static final long MAX_BYTES = 100;
    private static final List<String> ALLOWED = List.of("image/png", "image/jpeg", "image/webp");

    @Mock
    ProfileService profileService;

    @Mock
    MediaStorage avatarStorage;

    AvatarServiceImpl service;

    private UUID userId;
    private UUID profileId;

    @BeforeEach
    void setUp() {
        service = new AvatarServiceImpl(profileService, avatarStorage, MAX_BYTES, ALLOWED);
        userId = UUID.randomUUID();
        profileId = UUID.randomUUID();
    }

    private Profile profile(String avatarKey) {
        return Profile.builder()
                .id(profileId)
                .userId(userId)
                .username("alice")
                .avatarKey(avatarKey)
                .build();
    }

    @Test
    void requestUpload_presignsKeyUnderProfile_withSubtypeExtension() {
        when(profileService.getOrProvision(userId, "alice")).thenReturn(profile(null));
        when(avatarStorage.presignUpload(any(), eq("image/png")))
                .thenReturn(new PresignedUpload("https://put", "k", Instant.now()));

        service.requestUpload(userId, "alice", "image/png");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(avatarStorage).presignUpload(keyCaptor.capture(), eq("image/png"));
        assertThat(keyCaptor.getValue())
                .startsWith("avatars/" + profileId + "/")
                .endsWith(".png");
    }

    @Test
    void requestUpload_rejectsUnsupportedType() {
        assertThatThrownBy(() -> service.requestUpload(userId, "alice", "application/pdf"))
                .isInstanceOf(InvalidAvatarException.class);
        verify(avatarStorage, never()).presignUpload(any(), any());
    }

    @Test
    void confirm_setsAvatar_andDeletesPreviousObject() {
        String previousKey = "avatars/" + profileId + "/old.png";
        String newKey = "avatars/" + profileId + "/new.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(previousKey));
        when(avatarStorage.head(newKey)).thenReturn(Optional.of(new StoredObject(50, "image/png")));
        when(profileService.setAvatar(userId, newKey)).thenReturn(profile(newKey));

        Profile result = service.confirm(userId, newKey);

        assertThat(result.getAvatarKey()).isEqualTo(newKey);
        verify(profileService).setAvatar(userId, newKey);
        verify(avatarStorage).delete(previousKey);
    }

    @Test
    void confirm_doesNotDelete_whenNoPreviousAvatar() {
        String newKey = "avatars/" + profileId + "/new.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(null));
        when(avatarStorage.head(newKey)).thenReturn(Optional.of(new StoredObject(50, "image/png")));
        when(profileService.setAvatar(userId, newKey)).thenReturn(profile(newKey));

        service.confirm(userId, newKey);

        verify(avatarStorage, never()).delete(any());
    }

    @Test
    void confirm_rejectsKeyNotOwnedByProfile() {
        when(profileService.getByUserId(userId)).thenReturn(profile(null));

        assertThatThrownBy(() -> service.confirm(userId, "avatars/" + UUID.randomUUID() + "/x.png"))
                .isInstanceOf(InvalidAvatarException.class);
        verify(profileService, never()).setAvatar(any(), any());
    }

    @Test
    void confirm_rejectsWhenObjectMissing() {
        String key = "avatars/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(null));
        when(avatarStorage.head(key)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(userId, key)).isInstanceOf(InvalidAvatarException.class);
    }

    @Test
    void confirm_rejectsOversizedObject() {
        String key = "avatars/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(null));
        when(avatarStorage.head(key)).thenReturn(Optional.of(new StoredObject(MAX_BYTES + 1, "image/png")));

        assertThatThrownBy(() -> service.confirm(userId, key)).isInstanceOf(InvalidAvatarException.class);
    }

    @Test
    void confirm_rejectsDisallowedStoredContentType() {
        String key = "avatars/" + profileId + "/x.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(null));
        when(avatarStorage.head(key)).thenReturn(Optional.of(new StoredObject(10, "application/pdf")));

        assertThatThrownBy(() -> service.confirm(userId, key)).isInstanceOf(InvalidAvatarException.class);
    }

    @Test
    void remove_clearsAndDeletes_whenAvatarPresent() {
        String key = "avatars/" + profileId + "/a.png";
        when(profileService.getByUserId(userId)).thenReturn(profile(key));
        when(profileService.setAvatar(userId, null)).thenReturn(profile(null));

        service.remove(userId);

        verify(profileService).setAvatar(userId, null);
        verify(avatarStorage).delete(key);
    }

    @Test
    void remove_isNoOp_whenNoAvatar() {
        when(profileService.getByUserId(userId)).thenReturn(profile(null));

        service.remove(userId);

        verify(profileService, never()).setAvatar(any(), any());
        verify(avatarStorage, never()).delete(any());
    }
}
