package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.locale;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.OpeningHours;
import com.beamcard.profile.domain.model.PriceItem;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileDeletionService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ProfileService.UpdateProfileCommand;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.rest.model.request.AffiliationRequest;
import com.beamcard.profile.rest.model.request.LocationRequest;
import com.beamcard.profile.rest.model.request.OpeningHoursRequest;
import com.beamcard.profile.rest.model.request.PriceItemRequest;
import com.beamcard.profile.rest.model.request.UpdateProfileRequest;
import com.beamcard.profile.rest.model.response.AwardResponse;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import com.beamcard.profile.rest.utils.AvatarUrlUtil;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
@Validated
@RequiredArgsConstructor
public class MeProfileController {

    private final ProfileService profileService;
    private final ProfileDeletionService profileDeletionService;
    private final LinkService linkService;
    private final AwardService awardService;
    private final MediaStorage mediaStorage;

    @GetMapping
    public ProfileResponse getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        Profile profile = profileService.getOrProvision(userId(jwt), username(jwt), locale(jwt));
        return toResponse(profile);
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
                        request.phone(),
                        location == null ? null : new Location(location.country(), location.city()),
                        toAffiliations(request.affiliations()),
                        request.activities(),
                        request.currency(),
                        toPriceItems(request.priceItems()),
                        request.accentColor()));
        return toResponse(profile);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyProfile(@AuthenticationPrincipal Jwt jwt) {
        profileDeletionService.deleteByUserId(userId(jwt));
    }

    private ProfileResponse toResponse(Profile profile) {
        return ProfileResponse.of(
                profile,
                linkService.listByProfileId(profile.getId()),
                AvatarUrlUtil.of(mediaStorage, profile),
                AwardResponse.listOf(awardService.listForDisplay(profile.getId())));
    }

    private static List<Affiliation> toAffiliations(List<AffiliationRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(r -> new Affiliation(
                        r.role(), r.organization(), r.address(), r.description(), toOpeningHours(r.openingHours())))
                .toList();
    }

    private static List<PriceItem> toPriceItems(List<PriceItemRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(r -> new PriceItem(r.name(), r.priceType(), r.amountMin(), r.amountMax(), r.durationMinutes()))
                .toList();
    }

    private static List<OpeningHours> toOpeningHours(List<OpeningHoursRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(r -> new OpeningHours(r.day(), r.open(), r.close()))
                .toList();
    }
}
