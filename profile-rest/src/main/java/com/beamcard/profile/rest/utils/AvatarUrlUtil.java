package com.beamcard.profile.rest.utils;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.storage.MediaStorage;

public final class AvatarUrlUtil {

    private AvatarUrlUtil() {}

    public static String of(MediaStorage mediaStorage, Profile profile) {
        return profile.getAvatarKey() == null ? null : mediaStorage.publicUrl(profile.getAvatarKey());
    }
}
