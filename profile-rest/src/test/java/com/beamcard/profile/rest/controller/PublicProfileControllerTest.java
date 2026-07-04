package com.beamcard.profile.rest.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.exception.ProfileNotFoundException;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ShowcaseService;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.rest.config.SecurityConfig;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicProfileController.class)
@Import(SecurityConfig.class)
class PublicProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProfileService profileService;

    @MockBean
    LinkService linkService;

    @MockBean
    AwardService awardService;

    @MockBean
    ShowcaseService showcaseService;

    @MockBean
    MediaStorage mediaStorage;

    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void getByUsername_returns200_withoutToken() throws Exception {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("alice")
                .displayName("Alice")
                .build();
        when(profileService.getByUsername(eq("alice"))).thenReturn(profile);

        mockMvc.perform(get("/profiles/@alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.display_name").value("Alice"));
    }

    @Test
    void getByUsername_returns404_problemDetail_whenUnknown() throws Exception {
        when(profileService.getByUsername(eq("ghost"))).thenThrow(new ProfileNotFoundException("ghost"));

        mockMvc.perform(get("/profiles/@ghost"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("profile_not_found"));
    }
}
