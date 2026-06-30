package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.rest.model.request.AwardUploadUrlRequest;
import com.beamcard.profile.rest.model.request.ConfirmAwardRequest;
import com.beamcard.profile.rest.model.request.ReorderAwardsRequest;
import com.beamcard.profile.rest.model.request.UpdateAwardRequest;
import com.beamcard.profile.rest.model.response.AwardResponse;
import com.beamcard.profile.rest.model.response.AwardUploadResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile/awards")
@Validated
@RequiredArgsConstructor
public class MeAwardsController {

    private final AwardService awardService;
    private final ProfileService profileService;

    @GetMapping
    public List<AwardResponse> list(@AuthenticationPrincipal Jwt jwt) {
        UUID profileId =
                profileService.getOrProvision(userId(jwt), username(jwt)).getId();
        return AwardResponse.listOf(awardService.listForDisplay(profileId));
    }

    @PostMapping("/upload-url")
    public AwardUploadResponse uploadUrl(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AwardUploadUrlRequest request) {
        return AwardUploadResponse.of(awardService.requestUpload(userId(jwt), username(jwt), request.contentType()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AwardResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ConfirmAwardRequest request) {
        return AwardResponse.of(awardService.create(userId(jwt), request.key()));
    }

    @PutMapping("/{awardId}")
    public AwardResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID awardId,
            @Valid @RequestBody UpdateAwardRequest request) {
        return AwardResponse.of(awardService.update(userId(jwt), awardId, request.description()));
    }

    @DeleteMapping("/{awardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID awardId) {
        awardService.delete(userId(jwt), awardId);
    }

    @PutMapping("/order")
    public List<AwardResponse> reorder(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReorderAwardsRequest request) {
        return AwardResponse.listOf(awardService.reorder(userId(jwt), request.ids()));
    }
}
