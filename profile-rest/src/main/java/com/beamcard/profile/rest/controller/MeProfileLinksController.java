package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.locale;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.LinkService.CreateLinkCommand;
import com.beamcard.profile.domain.service.LinkService.UpdateLinkCommand;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.rest.model.request.CreateLinkRequest;
import com.beamcard.profile.rest.model.request.ReorderLinksRequest;
import com.beamcard.profile.rest.model.request.UpdateLinkRequest;
import com.beamcard.profile.rest.model.response.LinkResponse;
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
@RequestMapping("/me/profile/links")
@Validated
@RequiredArgsConstructor
public class MeProfileLinksController {

    private final LinkService linkService;
    private final ProfileService profileService;

    @GetMapping
    public List<LinkResponse> list(@AuthenticationPrincipal Jwt jwt) {
        UUID profileId = profileService
                .getOrProvision(userId(jwt), username(jwt), locale(jwt))
                .getId();
        return linkService.listByProfileId(profileId).stream()
                .map(LinkResponse::of)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LinkResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateLinkRequest request) {
        return LinkResponse.of(linkService.create(
                userId(jwt), username(jwt), new CreateLinkCommand(request.label(), request.url(), request.type())));
    }

    @PutMapping("/{linkId}")
    public LinkResponse update(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID linkId,
            @Valid @RequestBody UpdateLinkRequest request) {
        return LinkResponse.of(linkService.update(
                userId(jwt), linkId, new UpdateLinkCommand(request.label(), request.url(), request.type())));
    }

    @DeleteMapping("/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID linkId) {
        linkService.delete(userId(jwt), linkId);
    }

    @PutMapping("/order")
    public List<LinkResponse> reorder(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReorderLinksRequest request) {
        return linkService.reorder(userId(jwt), request.ids()).stream()
                .map(LinkResponse::of)
                .toList();
    }
}
