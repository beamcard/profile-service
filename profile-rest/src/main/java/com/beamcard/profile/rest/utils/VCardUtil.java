package com.beamcard.profile.rest.utils;

import static org.springframework.util.StringUtils.hasText;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import java.util.List;

/**
 * Builds a vCard 3.0 - the most broadly supported version across iOS and Android contacts.
 */
public final class VCardUtil {

    private static final String CRLF = "\r\n";

    private VCardUtil() {}

    public static String toVCard(Profile profile, List<Link> links, String avatarUrl) {
        String name = hasText(profile.getDisplayName()) ? profile.getDisplayName() : profile.getUsername();

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD").append(CRLF);
        sb.append("VERSION:3.0").append(CRLF);
        sb.append("FN:").append(escape(name)).append(CRLF);
        sb.append("N:").append(structuredName(name)).append(CRLF);

        if (hasText(profile.getBio())) {
            sb.append("NOTE:").append(escape(profile.getBio())).append(CRLF);
        }

        String adr = address(profile);
        if (adr != null) {
            sb.append(adr).append(CRLF);
        }

        for (Link link : links) {
            if (link.getType() == LinkType.EMAIL) {
                sb.append("EMAIL;TYPE=INTERNET:")
                        .append(escape(stripMailto(link.getUrl())))
                        .append(CRLF);
            } else {
                sb.append("URL:").append(escape(link.getUrl())).append(CRLF);
            }
        }

        if (avatarUrl != null) {
            sb.append("PHOTO;VALUE=URI:").append(escape(avatarUrl)).append(CRLF);
        }

        sb.append("END:VCARD").append(CRLF);
        return sb.toString();
    }

    /** N is Family;Given;Additional;Prefix;Suffix — best-effort split of the display name. */
    private static String structuredName(String name) {
        String[] parts = name.trim().split("\\s+", 2);
        String given = escape(parts[0]);
        String family = parts.length > 1 ? escape(parts[1]) : "";
        return family + ";" + given + ";;;";
    }

    private static String address(Profile profile) {
        Location location = profile.getLocation();
        if (location == null || location.isEmpty()) {
            return null;
        }
        String street = blankToEmpty(location.address());
        String city = blankToEmpty(location.city());
        String country = blankToEmpty(location.country());
        return "ADR;TYPE=WORK:;;" + escape(street) + ";" + escape(city) + ";;;" + escape(country);
    }

    private static String blankToEmpty(String value) {
        return hasText(value) ? value.trim() : "";
    }

    private static String stripMailto(String value) {
        return value != null && value.regionMatches(true, 0, "mailto:", 0, 7) ? value.substring(7) : value;
    }

    /** Escape per RFC 6350/2426: backslash, newline, comma, semicolon. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace(",", "\\,")
                .replace(";", "\\;");
    }
}
