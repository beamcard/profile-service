package com.beamcard.profile.rest.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.exception.InvalidAvatarException;
import com.beamcard.profile.domain.service.AvatarService;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.storage.MediaStorage;
import com.beamcard.profile.domain.storage.MediaStorage.PresignedUpload;
import com.beamcard.profile.rest.config.SecurityConfig;
import java.time.Instant;
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

@WebMvcTest(MeAvatarController.class)
@Import(SecurityConfig.class)
class MeAvatarControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AvatarService avatarService;

    @MockBean
    AwardService awardService;

    @MockBean
    LinkService linkService;

    @MockBean
    MediaStorage mediaStorage;

    @MockBean
    JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.randomUUID();

    private static RequestPostProcessor aliceToken() {
        return jwt().jwt(j -> j.subject(USER_ID.toString()).claim("username", "alice"));
    }

    @Test
    void uploadUrl_returnsPresignedTarget() throws Exception {
        when(avatarService.requestUpload(eq(USER_ID), eq("alice"), eq("image/png")))
                .thenReturn(new PresignedUpload("https://minio/put-here", "avatars/p/x.png", Instant.now()));

        mockMvc.perform(post("/me/profile/avatar/upload-url")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upload_url").value("https://minio/put-here"))
                .andExpect(jsonPath("$.key").value("avatars/p/x.png"));
    }

    @Test
    void uploadUrl_returns400_whenTypeRejected() throws Exception {
        when(avatarService.requestUpload(eq(USER_ID), eq("alice"), eq("application/pdf")))
                .thenThrow(new InvalidAvatarException("Unsupported image type: application/pdf"));

        mockMvc.perform(post("/me/profile/avatar/upload-url")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content_type\":\"application/pdf\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid_avatar"));
    }

    @Test
    void uploadUrl_returns401_withoutToken() throws Exception {
        mockMvc.perform(post("/me/profile/avatar/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isUnauthorized());
    }
}
