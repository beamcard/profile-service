package com.beamcard.profile.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
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

@WebMvcTest(MeProfileController.class)
@Import(SecurityConfig.class)
class MeProfileControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ProfileService profileService;

    @MockBean
    LinkService linkService;

    @MockBean
    JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.randomUUID();

    private static org.springframework.test.web.servlet.request.RequestPostProcessor aliceToken() {
        return jwt().jwt(j -> j.subject(USER_ID.toString()).claim("username", "alice"));
    }

    @Test
    void getMe_returns200_andProvisionsFromToken() throws Exception {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .username("alice")
                .displayName("Alice")
                .build();
        when(profileService.getOrProvision(eq(USER_ID), eq("alice"))).thenReturn(profile);

        mockMvc.perform(get("/me/profile").with(aliceToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.display_name").value("Alice"));
    }

    @Test
    void getMe_returns401_withoutToken() throws Exception {
        mockMvc.perform(get("/me/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void putMe_updatesFromToken_andBody() throws Exception {
        Profile updated = Profile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .username("alice")
                .displayName("New name")
                .build();
        when(profileService.update(eq(USER_ID), eq("alice"), any())).thenReturn(updated);

        mockMvc.perform(put("/me/profile")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"display_name\":\"New name\",\"bio\":\"Guide\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.display_name").value("New name"));
    }

    @Test
    void putMe_returns400_problemDetail_whenValidationFails() throws Exception {
        String tooLongName = "x".repeat(81);

        mockMvc.perform(put("/me/profile")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"display_name\":\"" + tooLongName + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.errors.displayName").exists());
    }
}
