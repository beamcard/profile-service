package com.beamcard.profile.domain.conf;

import com.beamcard.profile.domain.repository.LinkRepository;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.domain.service.AvatarService;
import com.beamcard.profile.domain.service.AvatarServiceImpl;
import com.beamcard.profile.domain.service.LinkService;
import com.beamcard.profile.domain.service.LinkServiceImpl;
import com.beamcard.profile.domain.service.ProfileService;
import com.beamcard.profile.domain.service.ProfileServiceImpl;
import com.beamcard.profile.domain.storage.AvatarStorage;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class DomainConfig {

    @Bean
    public ProfileService profileService(ProfileRepository profileRepository) {
        return new ProfileServiceImpl(profileRepository);
    }

    @Bean
    public LinkService linkService(LinkRepository linkRepository, ProfileService profileService) {
        return new LinkServiceImpl(linkRepository, profileService);
    }

    @Bean
    public AvatarService avatarService(
            ProfileService profileService,
            AvatarStorage avatarStorage,
            @Value("${beamcard.avatar.max-size-bytes}") long maxSizeBytes,
            @Value("${beamcard.avatar.allowed-content-types}") List<String> allowedContentTypes) {
        return new AvatarServiceImpl(profileService, avatarStorage, maxSizeBytes, allowedContentTypes);
    }
}
