package com.beamcard.profile.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.domain.exception.AwardNotFoundException;
import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.service.AwardService;
import com.beamcard.profile.domain.service.AwardService.AwardView;
import com.beamcard.profile.domain.service.ProfileService;
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

@WebMvcTest(MeAwardsController.class)
@Import(SecurityConfig.class)
class MeAwardsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AwardService awardService;

    @MockBean
    ProfileService profileService;

    @MockBean
    JwtDecoder jwtDecoder;

    private static final UUID USER_ID = UUID.randomUUID();

    private static RequestPostProcessor aliceToken() {
        return jwt().jwt(j -> j.subject(USER_ID.toString()).claim("username", "alice"));
    }

    @Test
    void uploadUrl_returnsPresignedTarget() throws Exception {
        when(awardService.requestUpload(eq(USER_ID), eq("alice"), eq("image/png")))
                .thenReturn(new PresignedUpload("https://minio/put-here", "awards/p/x.png", Instant.now()));

        mockMvc.perform(post("/me/profile/awards/upload-url")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upload_url").value("https://minio/put-here"))
                .andExpect(jsonPath("$.key").value("awards/p/x.png"));
    }

    @Test
    void create_returns201_withPublicImageUrl() throws Exception {
        UUID awardId = UUID.randomUUID();
        String key = "awards/p/x.png";
        Award award = Award.builder().id(awardId).imageKey(key).position(1).build();
        when(awardService.create(eq(USER_ID), eq(key))).thenReturn(new AwardView(award, "https://cdn/" + key));

        mockMvc.perform(post("/me/profile/awards")
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"" + key + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(awardId.toString()))
                .andExpect(jsonPath("$.image_url").value("https://cdn/" + key))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void update_returns200_withDescription() throws Exception {
        UUID awardId = UUID.randomUUID();
        String key = "awards/p/x.png";
        Award award = Award.builder()
                .id(awardId)
                .imageKey(key)
                .description("Board Certification")
                .position(1)
                .build();
        when(awardService.update(eq(USER_ID), eq(awardId), eq("Board Certification")))
                .thenReturn(new AwardView(award, "https://cdn/" + key));

        mockMvc.perform(put("/me/profile/awards/" + awardId)
                        .with(aliceToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Board Certification\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(awardId.toString()))
                .andExpect(jsonPath("$.description").value("Board Certification"));
    }

    @Test
    void delete_returns204() throws Exception {
        UUID awardId = UUID.randomUUID();

        mockMvc.perform(delete("/me/profile/awards/" + awardId).with(aliceToken()))
                .andExpect(status().isNoContent());

        verify(awardService).delete(USER_ID, awardId);
    }

    @Test
    void delete_returns404_problemDetail_whenAwardUnknown() throws Exception {
        UUID awardId = UUID.randomUUID();
        org.mockito.Mockito.doThrow(new AwardNotFoundException(awardId))
                .when(awardService)
                .delete(eq(USER_ID), eq(awardId));

        mockMvc.perform(delete("/me/profile/awards/" + awardId).with(aliceToken()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("award_not_found"));
    }

    @Test
    void uploadUrl_returns401_withoutToken() throws Exception {
        mockMvc.perform(post("/me/profile/awards/upload-url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content_type\":\"image/png\"}"))
                .andExpect(status().isUnauthorized());
        verify(awardService, org.mockito.Mockito.never()).requestUpload(any(), any(), any());
    }
}
