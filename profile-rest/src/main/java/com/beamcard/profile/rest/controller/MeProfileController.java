package com.beamcard.profile.rest.controller;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ProfileService.UpdateProfileCommand;
import com.beamcard.profile.rest.model.request.UpdateProfileRequest;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@Validated
@RequiredArgsConstructor
public class MeProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        Profile profile = profileService.getOrProvision(userId(jwt), username(jwt));
        return ProfileResponse.of(profile);
    }

    @PutMapping
    public ProfileResponse updateMyProfile(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        Profile profile = profileService.update(
                userId(jwt), username(jwt), new UpdateProfileCommand(request.displayName(), request.bio()));
        return ProfileResponse.of(profile);
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }

    private static String username(Jwt jwt) {
        return jwt.getClaimAsString("username");
    }
}
