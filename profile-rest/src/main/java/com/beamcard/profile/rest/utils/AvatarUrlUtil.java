package com.beamcard.profile.rest.utils;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.storage.AvatarStorage;

public final class AvatarUrlUtil {

    private AvatarUrlUtil() {}

    public static String of(AvatarStorage avatarStorage, Profile profile) {
        return profile.getAvatarKey() == null ? null : avatarStorage.publicUrl(profile.getAvatarKey());
    }
}
