package com.beamcard.profile.rest.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.beamcard.profile.rest.config.SecurityConfig;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(MeProfileQrController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "beamcard.web.public-url=https://beam.test")
class MeProfileQrControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtDecoder jwtDecoder;

    private static RequestPostProcessor aliceToken() {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString()).claim("username", "alice"));
    }

    @Test
    void qr_returnsSvg_forAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/me/profile/qr").with(aliceToken()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<svg")));
    }

    @Test
    void qr_returns401_withoutToken() throws Exception {
        mockMvc.perform(get("/me/profile/qr")).andExpect(status().isUnauthorized());
    }
}
