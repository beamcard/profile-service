package com.beamcard.profile.domain.service;

import com.beamcard.profile.domain.exception.OverlappingHoursException;
import com.beamcard.profile.domain.exception.ProfileNotFoundException;
import com.beamcard.profile.domain.model.Affiliation;
import com.beamcard.profile.domain.model.OpeningHours;
import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public Profile getOrProvision(UUID userId, String username) {
        return getOrProvision(userId, username, null);
    }

    @Override
    @Transactional
    public Profile getOrProvision(UUID userId, String username, String locale) {
        return profileRepository
                .findByUserId(userId)
                .map(existing -> syncHandleAndLocale(existing, username, locale))
                .orElseGet(() -> provision(userId, username, locale));
    }

    private Profile syncHandleAndLocale(Profile existing, String username, String locale) {
        boolean usernameChanged = username != null && !username.equals(existing.getUsername());
        boolean localeChanged = locale != null && !locale.equals(existing.getLocale());
        if (!usernameChanged && !localeChanged) {
            return existing;
        }
        Profile.ProfileBuilder builder = existing.toBuilder();
        if (usernameChanged) {
            builder.username(username);
        }
        if (localeChanged) {
            builder.locale(locale);
        }
        Profile updated = profileRepository.save(builder.build());
        log.debug("Reconciled profile {} handle/locale from token for user {}", updated.getId(), existing.getUserId());
        return updated;
    }

    @Override
    @Transactional(readOnly = true)
    public Profile getByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).orElseThrow(() -> new ProfileNotFoundException(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public Profile getByUsername(String username) {
        return profileRepository.findByUsername(username).orElseThrow(() -> new ProfileNotFoundException(username));
    }

    @Override
    @Transactional
    public Profile update(UUID userId, String username, UpdateProfileCommand command) {
        Profile current = getOrProvision(userId, username);

        Profile.ProfileBuilder builder = current.toBuilder();
        if (command.displayName() != null) {
            builder.displayName(command.displayName());
        }
        if (command.bio() != null) {
            builder.bio(command.bio());
        }
        if (command.phone() != null) {
            String phone = command.phone().trim();
            builder.phone(phone.isEmpty() ? null : phone);
        }
        if (command.location() != null) {
            builder.location(command.location());
        }
        if (command.affiliations() != null) {
            validateOpeningHours(command.affiliations());
            validateNoOverlappingHours(command.affiliations());
            builder.affiliations(command.affiliations());
        }
        if (command.activities() != null) {
            builder.activities(command.activities());
        }
        if (command.currency() != null) {
            builder.currency(command.currency());
        }
        if (command.priceItems() != null) {
            builder.priceItems(command.priceItems());
        }
        if (command.accentColor() != null) {
            builder.accentColor(command.accentColor());
        }
        return profileRepository.save(builder.build());
    }

    private static void validateOpeningHours(List<Affiliation> affiliations) {
        extractOpeningHours(affiliations).forEach(OpeningHours::validate);
    }

    /**
     * One person can't be in two places at once: no two intervals may overlap on the same
     * weekday, within or across workplaces. Pairwise check — the interval count is tiny.
     */
    private static void validateNoOverlappingHours(List<Affiliation> affiliations) {
        List<OpeningHours> hours = extractOpeningHours(affiliations).toList();
        for (int i = 0; i < hours.size(); i++) {
            for (int j = i + 1; j < hours.size(); j++) {
                if (hours.get(i).overlaps(hours.get(j))) {
                    throw new OverlappingHoursException(
                            "Opening hours overlap on " + hours.get(i).day());
                }
            }
        }
    }

    private static Stream<OpeningHours> extractOpeningHours(List<Affiliation> affiliations) {
        return affiliations.stream()
                .filter(Objects::nonNull)
                .map(Affiliation::openingHours)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(Objects::nonNull);
    }

    @Override
    @Transactional
    public Profile setAvatar(UUID userId, String avatarKey) {
        Profile current = getByUserId(userId);
        return profileRepository.save(current.toBuilder().avatarKey(avatarKey).build());
    }

    private Profile provision(UUID userId, String username, String locale) {
        Profile created = profileRepository.save(Profile.builder()
                .userId(userId)
                .username(username)
                .locale(locale != null ? locale : "en")
                .build());
        log.info("Provisioned profile {} for user {} (@{})", created.getId(), userId, username);
        return created;
    }
}
