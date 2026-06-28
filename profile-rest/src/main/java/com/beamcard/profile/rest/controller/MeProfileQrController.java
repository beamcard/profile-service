package com.beamcard.profile.rest.controller;

import static com.beamcard.profile.rest.utils.JwtClaimsUtil.username;

import com.beamcard.profile.rest.utils.QrSvgUtil;
import io.nayuki.qrcodegen.QrCode;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me/profile")
public class MeProfileQrController {

    private static final MediaType SVG = MediaType.valueOf("image/svg+xml");

    private final String webPublicUrl;

    public MeProfileQrController(@Value("${beamcard.web.public-url}") String webPublicUrl) {
        this.webPublicUrl = webPublicUrl;
    }

    @GetMapping(value = "/qr", produces = "image/svg+xml")
    public ResponseEntity<String> qr(@AuthenticationPrincipal Jwt jwt) {
        String target = "%s/@%s?channel=qr".formatted(webPublicUrl, username(jwt));
        QrCode qr = QrCode.encodeText(target, QrCode.Ecc.MEDIUM);
        return ResponseEntity.ok()
                .contentType(SVG)
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                .body(QrSvgUtil.toSvg(qr));
    }
}
