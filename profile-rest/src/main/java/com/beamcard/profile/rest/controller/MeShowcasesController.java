package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.locale;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.userId;
import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.domain.model.Showcase;
import com.beamcard.profile.domain.model.ShowcaseStep;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ShowcaseService;
import com.beamcard.profile.rest.model.request.SaveShowcasesRequest;
import com.beamcard.profile.rest.model.request.ShowcaseRequest;
import com.beamcard.profile.rest.model.request.ShowcaseUploadUrlRequest;
import com.beamcard.profile.rest.model.response.ShowcaseResponse;
import com.beamcard.profile.rest.model.response.ShowcaseUploadResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile/showcases")
@Validated
@RequiredArgsConstructor
public class MeShowcasesController {

    private final ShowcaseService showcaseService;
    private final ProfileService profileService;

    @GetMapping
    public List<ShowcaseResponse> list(@AuthenticationPrincipal Jwt jwt) {
        var profileId = profileService
                .getOrProvision(userId(jwt), username(jwt), locale(jwt))
                .getId();
        return ShowcaseResponse.listOf(showcaseService.listForDisplay(profileId));
    }

    @PutMapping
    public List<ShowcaseResponse> save(
            @AuthenticationPrincipal Jwt jwt, @Valid @NotNull @RequestBody SaveShowcasesRequest request) {
        List<Showcase> showcases = request.showcases() == null
                ? List.of()
                : request.showcases().stream()
                        .map(MeShowcasesController::toShowcase)
                        .toList();
        return ShowcaseResponse.listOf(showcaseService.replace(userId(jwt), showcases));
    }

    @PostMapping("/upload-url")
    public ShowcaseUploadResponse uploadUrl(
            @AuthenticationPrincipal Jwt jwt, @Valid @NotNull @RequestBody ShowcaseUploadUrlRequest request) {
        return ShowcaseUploadResponse.of(
                showcaseService.requestUpload(userId(jwt), username(jwt), request.contentType()));
    }

    private static Showcase toShowcase(ShowcaseRequest request) {
        List<ShowcaseStep> steps = request.steps() == null
                ? List.of()
                : request.steps().stream()
                        .map(step -> new ShowcaseStep(step.imageKey(), step.description()))
                        .toList();
        return new Showcase(request.title(), request.intro(), steps);
    }
}
