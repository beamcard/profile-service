package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ProfileService.UpdateProfileCommand;
import com.beamcard.profile.domain.storage.AvatarStorage;
import com.beamcard.profile.rest.model.request.AffiliationRequest;
import com.beamcard.profile.rest.model.request.LocationRequest;
import com.beamcard.profile.rest.model.request.UpdateProfileRequest;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import com.beamcard.profile.rest.utils.AvatarUrlUtil;
import jakarta.validation.Valid;
import java.util.List;
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
    private final LinkService linkService;
    private final AvatarStorage avatarStorage;

    @GetMapping
    public ProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        Profile profile = profileService.getOrProvision(userId(jwt), username(jwt));
        return ProfileResponse.of(
                profile, linkService.listByProfileId(profile.getId()), AvatarUrlUtil.of(avatarStorage, profile));
    }

    @PutMapping
    public ProfileResponse updateMyProfile(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        LocationRequest location = request.location();
        Profile profile = profileService.update(
                userId(jwt),
                username(jwt),
                new UpdateProfileCommand(
                        request.displayName(),
                        request.bio(),
                        location == null ? null : new Location(location.country(), location.city()),
                        toAffiliations(request.affiliations())));
        return ProfileResponse.of(
                profile, linkService.listByProfileId(profile.getId()), AvatarUrlUtil.of(avatarStorage, profile));
    }

    private static List<Affiliation> toAffiliations(List<AffiliationRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(r -> new Affiliation(r.role(), r.organization(), r.address(), r.description()))
                .toList();
    }
}
