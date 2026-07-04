package com.beamcard.profile.rest.controller;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ShowcaseService;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.rest.model.response.AwardResponse;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import com.beamcard.profile.rest.model.response.ShowcaseResponse;
import com.beamcard.profile.rest.utils.AvatarUrlUtil;
import com.beamcard.profile.rest.utils.VCardUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class PublicProfileController {

    private static final MediaType VCARD = MediaType.valueOf("text/vcard;charset=utf-8");

    private final ProfileService profileService;
    private final LinkService linkService;
    private final AwardService awardService;
    private final ShowcaseService showcaseService;
    private final MediaStorage mediaStorage;

    @GetMapping("/@{username}")
    public ProfileResponse getByUsername(@PathVariable String username) {
        Profile profile = profileService.getByUsername(username);
        return ProfileResponse.of(
                profile,
                linkService.listByProfileId(profile.getId()),
                AvatarUrlUtil.of(mediaStorage, profile),
                AwardResponse.listOf(awardService.listForDisplay(profile.getId())));
    }

    @GetMapping("/@{username}/showcases")
    public List<ShowcaseResponse> getShowcases(@PathVariable String username) {
        Profile profile = profileService.getByUsername(username);
        return ShowcaseResponse.listOf(showcaseService.listForDisplay(profile.getId()));
    }

    @GetMapping(value = "/@{username}/vcard", produces = "text/vcard;charset=utf-8")
    public ResponseEntity<String> getProfileVcard(@PathVariable String username) {
        Profile profile = profileService.getByUsername(username);
        String vcf = VCardUtil.toVCard(
                profile, linkService.listByProfileId(profile.getId()), AvatarUrlUtil.of(mediaStorage, profile));
        return ResponseEntity.ok()
                .contentType(VCARD)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + username + ".vcf\"")
                .body(vcf);
    }
}
