package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.AvatarService;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.rest.model.request.AvatarUploadUrlRequest;
import com.beamcard.profile.rest.model.request.ConfirmAvatarRequest;
import com.beamcard.profile.rest.model.response.AvatarUploadResponse;
import com.beamcard.profile.rest.model.response.AwardResponse;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import com.beamcard.profile.rest.utils.AvatarUrlUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile/avatar")
@Validated
@RequiredArgsConstructor
public class MeAvatarController {

    private final AvatarService avatarService;
    private final AwardService awardService;
    private final LinkService linkService;
    private final MediaStorage mediaStorage;

    @PostMapping("/upload-url")
    public AvatarUploadResponse uploadUrl(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AvatarUploadUrlRequest request) {
        return AvatarUploadResponse.of(avatarService.requestUpload(userId(jwt), username(jwt), request.contentType()));
    }

    @PutMapping
    public ProfileResponse confirm(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfirmAvatarRequest request) {
        return toResponse(avatarService.confirm(userId(jwt), request.key()));
    }

    @DeleteMapping
    public ProfileResponse remove(@AuthenticationPrincipal Jwt jwt) {
        return toResponse(avatarService.remove(userId(jwt)));
    }

    private ProfileResponse toResponse(Profile profile) {
        return ProfileResponse.of(
                profile,
                linkService.listByProfileId(profile.getId()),
                AvatarUrlUtil.of(mediaStorage, profile),
                AwardResponse.listOf(awardService.listForDisplay(profile.getId())));
    }
}
