package com.beamcard.profile.rest.controller;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.rest.model.response.ProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class PublicProfileController {

    private final ProfileService profileService;
    private final LinkService linkService;

    @GetMapping("/@{username}")
    public ProfileResponse getByUsername(@PathVariable String username) {
        Profile profile = profileService.getByUsername(username);
        return ProfileResponse.of(profile, linkService.listByProfileId(profile.getId()));
    }
}
