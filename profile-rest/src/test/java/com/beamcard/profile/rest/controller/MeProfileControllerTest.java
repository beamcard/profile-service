package com.beamcard.profile.rest.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ProfileService.UpdateProfileCommand;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.rest.config.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    AwardService awardService;

    @MockBean
    MediaStorage mediaStorage;

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
    void putMe_acceptsPrimaryLocationAndAffiliations_andReturnsThem() throws Exception {
        Profile updated = Profile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .username("alice")
                .location(new Location("Austria", "Vienna"))
                .affiliations(List.of(new Affiliation("Trainer", "FitGym", "Stephansplatz 1", "Entrance B")))
                .build();
        when(profileService.update(eq(USER_ID), eq("alice"), any())).thenReturn(updated);

        mockMvc.perform(
                        put("/me/profile")
                                .with(aliceToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"location\":{\"country\":\"Austria\",\"city\":\"Vienna\"},\"affiliations\":[{\"role\":\"Trainer\",\"organization\":\"FitGym\",\"address\":\"Stephansplatz 1\",\"description\":\"Entrance B\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location.country").value("Austria"))
                .andExpect(jsonPath("$.location.city").value("Vienna"))
                .andExpect(jsonPath("$.affiliations[0].role").value("Trainer"))
                .andExpect(jsonPath("$.affiliations[0].organization").value("FitGym"))
                .andExpect(jsonPath("$.affiliations[0].address").value("Stephansplatz 1"))
                .andExpect(jsonPath("$.affiliations[0].description").value("Entrance B"));

        ArgumentCaptor<UpdateProfileCommand> cmd = ArgumentCaptor.forClass(UpdateProfileCommand.class);
        verify(profileService).update(eq(USER_ID), eq("alice"), cmd.capture());
        assertThat(cmd.getValue().location().city()).isEqualTo("Vienna");
        assertThat(cmd.getValue().affiliations()).hasSize(1);
        assertThat(cmd.getValue().affiliations().getFirst().address()).isEqualTo("Stephansplatz 1");
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
