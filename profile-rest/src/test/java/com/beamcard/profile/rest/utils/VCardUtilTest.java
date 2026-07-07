package com.beamcard.profile.rest.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.model.LinkType;
import com.beamcard.profile.domain.model.Location;
import com.beamcard.profile.domain.model.Profile;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VCardUtilTest {

    private Profile profile(String displayName, String bio) {
        return Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("alice")
                .displayName(displayName)
                .bio(bio)
                .build();
    }

    private Profile profileWith(Location location, Affiliation... affiliations) {
        return Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("alice")
                .displayName("Alice")
                .location(location)
                .affiliations(List.of(affiliations))
                .build();
    }

    private Link link(LinkType type, String url) {
        return Link.builder()
                .id(UUID.randomUUID())
                .label(type.name())
                .url(url)
                .type(type)
                .position(0)
                .build();
    }

    @Test
    void buildsWellFormedVcard_withNameBioLinksAndPhoto() {
        String vcf = VCardUtil.toVCard(
                profile("Alice Smith", "Mountain guide"),
                List.of(
                        link(LinkType.EMAIL, "alice@example.com"),
                        link(LinkType.INSTAGRAM, "https://instagram.com/alice")),
                "https://cdn.example/a.png");

        assertThat(vcf).startsWith("BEGIN:VCARD\r\nVERSION:3.0\r\n");
        assertThat(vcf).endsWith("END:VCARD\r\n");
        assertThat(vcf).contains("FN:Alice Smith\r\n");
        assertThat(vcf).contains("N:Smith;Alice;;;\r\n");
        assertThat(vcf).contains("NOTE:Mountain guide\r\n");
        assertThat(vcf).contains("EMAIL;TYPE=INTERNET:alice@example.com\r\n");
        assertThat(vcf).contains("URL:https://instagram.com/alice\r\n");
        assertThat(vcf).contains("PHOTO;VALUE=URI:https://cdn.example/a.png\r\n");
    }

    @Test
    void fallsBackToUsername_whenNoDisplayName_andOmitsOptionalFields() {
        String vcf = VCardUtil.toVCard(profile(null, null), List.of(), null);

        assertThat(vcf).contains("FN:alice\r\n");
        assertThat(vcf).contains("N:;alice;;;\r\n");
        assertThat(vcf).doesNotContain("NOTE:");
        assertThat(vcf).doesNotContain("PHOTO");
        assertThat(vcf).doesNotContain("TEL");
    }

    @Test
    void emitsTel_whenPhonePresent() {
        Profile profile = Profile.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("alice")
                .displayName("Alice")
                .phone("+380671234567")
                .build();

        String vcf = VCardUtil.toVCard(profile, List.of(), null);

        assertThat(vcf).contains("TEL;TYPE=CELL:+380671234567\r\n");
    }

    @Test
    void usesPrimaryAffiliationForOrgAndTitle() {
        Profile profile = profileWith(
                null,
                new Affiliation("Product Designer", "Acme", null, null),
                new Affiliation("Advisor", "OtherCo", null, null));

        String vcf = VCardUtil.toVCard(profile, List.of(), null);

        assertThat(vcf).contains("ORG:Acme\r\n");
        assertThat(vcf).contains("TITLE:Product Designer\r\n");
        assertThat(vcf).doesNotContain("OtherCo");
    }

    @Test
    void emitsWorkAddressPerWorkplace_withSharedPrimaryCityCountry() {
        Profile profile = profileWith(
                new Location("Austria", "Vienna"),
                new Affiliation("Trainer", "FitGym", "Stephansplatz 1", null),
                new Affiliation("Trainer", "PowerHouse", "Hauptplatz 2", null));

        String vcf = VCardUtil.toVCard(profile, List.of(), null);

        // Each workplace street + the profile's shared Vienna, Austria.
        assertThat(vcf).contains("ADR;TYPE=WORK:;;Stephansplatz 1;Vienna;;;Austria\r\n");
        assertThat(vcf).contains("ADR;TYPE=WORK:;;Hauptplatz 2;Vienna;;;Austria\r\n");
        assertThat(vcf.split("ADR;TYPE=WORK:", -1)).hasSize(3);
    }

    @Test
    void omitsAddress_whenNoStreetAndNoPrimaryLocation() {
        String vcf = VCardUtil.toVCard(profileWith(null, new Affiliation("Dev", "Acme", null, null)), List.of(), null);

        assertThat(vcf).doesNotContain("ADR");
    }

    @Test
    void stripsMailtoPrefix_onEmailLinks() {
        String vcf =
                VCardUtil.toVCard(profile("Bob", null), List.of(link(LinkType.EMAIL, "mailto:bob@example.com")), null);

        assertThat(vcf).contains("EMAIL;TYPE=INTERNET:bob@example.com\r\n");
        assertThat(vcf).doesNotContain("mailto:");
    }

    @Test
    void escapesSpecialCharactersInTextFields() {
        String vcf = VCardUtil.toVCard(profile("Ann; Lee, Jr.", "line1\nline2"), List.of(), null);

        assertThat(vcf).contains("FN:Ann\\; Lee\\, Jr.\r\n");
        assertThat(vcf).contains("NOTE:line1\\nline2\r\n");
    }
}
