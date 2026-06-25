package com.beamcard.profile.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.exception.LinkNotFoundException;
import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.rest.config.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(MeProfileLinksController.class)
@Import(SecurityConfig.class)
class MeProfileLinksControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    LinkService linkService;

    @MockBean
    ProfileService profileService;

    @MockBean
    JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.randomUUID();

    private static RequestPostProcessor aliceToken() {
        return jwt().jwt(j -> j.subject(USER_ID.toString()).claim("username", "alice"));
    }

    private static Link sampleLink(UUID id) {
        return Link.builder()
                .id(id)
                .profileId(UUID.randomUUID())
                .label("Website")
                .url("https://example.com")
                .type(LinkType.GENERIC)
                .position(0)
                .build();
    }

    @Test
    void create_returns201_withLink() throws Exception {
        UUID id = UUID.randomUUID();
        when(linkService.create(eq(USER_ID), eq("alice"), any())).thenReturn(sampleLink(id));

        mockMvc.perform(post("/me/profile/links")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Website\",\"url\":\"https://example.com\",\"type\":\"GENERIC\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.label").value("Website"))
                .andExpect(jsonPath("$.type").value("GENERIC"))
                .andExpect(jsonPath("$.position").value(0));
    }

    @Test
    void create_returns400_whenUrlInvalidForType() throws Exception {
        mockMvc.perform(post("/me/profile/links")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"IG\",\"url\":\"https://example.com\",\"type\":\"INSTAGRAM\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("validation_failed"))
                .andExpect(jsonPath("$.errors.url").exists());
    }

    @Test
    void list_returns200() throws Exception {
        UUID profileId = UUID.randomUUID();
        Profile profile = Profile.builder()
                .id(profileId)
                .userId(USER_ID)
                .username("alice")
                .build();
        when(profileService.getOrProvision(eq(USER_ID), eq("alice"))).thenReturn(profile);
        when(linkService.listByProfileId(eq(profileId))).thenReturn(List.of(sampleLink(UUID.randomUUID())));

        mockMvc.perform(get("/me/profile/links").with(aliceToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].label").value("Website"));
    }

    @Test
    void update_returns404_whenLinkNotOwned() throws Exception {
        UUID id = UUID.randomUUID();
        when(linkService.update(eq(USER_ID), eq(id), any())).thenThrow(new LinkNotFoundException(id));

        mockMvc.perform(put("/me/profile/links/" + id)
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"label\":\"Renamed\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("link_not_found"));
    }

    @Test
    void delete_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/me/profile/links/" + id).with(aliceToken())).andExpect(status().isNoContent());

        verify(linkService).delete(eq(USER_ID), eq(id));
    }

    @Test
    void list_returns401_withoutToken() throws Exception {
        mockMvc.perform(get("/me/profile/links")).andExpect(status().isUnauthorized());
    }
}
