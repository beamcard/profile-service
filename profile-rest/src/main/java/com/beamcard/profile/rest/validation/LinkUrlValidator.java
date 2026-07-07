package com.beamcard.profile.rest.validation;

import com.beamcard.profile.domain.model.LinkType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

public class LinkUrlValidator implements ConstraintValidator<ValidLink, LinkUrlForm> {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{6,15}$");

    @Override
    public boolean isValid(LinkUrlForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        String url = form.url();
        LinkType type = form.type();
        if (url == null || type == null) {
            return true;
        }

        boolean valid =
                switch (type) {
                    case EMAIL -> EMAIL.matcher(url).matches();
                    case WHATSAPP -> PHONE.matcher(url).matches() || hostEndsWith(url, "wa.me", "whatsapp.com");
                    case INSTAGRAM -> hostEndsWith(url, "instagram.com");
                    case TELEGRAM -> hostEndsWith(url, "t.me", "telegram.me", "telegram.org");
                    case TWITTER -> hostEndsWith(url, "twitter.com", "x.com");
                    case LINKEDIN -> hostEndsWith(url, "linkedin.com");
                    case GENERIC -> isHttpUrl(url);
                };

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("url")
                    .addConstraintViolation();
        }
        return valid;
    }

    private static boolean isHttpUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme();
            return uri.getHost() != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean hostEndsWith(String value, String... domains) {
        if (!isHttpUrl(value)) {
            return false;
        }
        String host = URI.create(value.trim()).getHost().toLowerCase(Locale.ROOT);
        for (String domain : domains) {
            if (host.equals(domain) || host.endsWith("." + domain)) {
                return true;
            }
        }
        return false;
    }
}
