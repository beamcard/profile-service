package com.beamcard.profile.persistence.config;

import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.repository.ProfileRepositoryImpl;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.beamcard.profile.persistence.repository.jpa")
public class PersistenceConfig {

    @Bean
    public ProfilePersistenceMapper profilePersistenceMapper() {
        return Mappers.getMapper(ProfilePersistenceMapper.class);
    }

    @Bean
    public ProfileRepository profileRepository(
            ProfileJpaRepository profileJpaRepository, ProfilePersistenceMapper profilePersistenceMapper) {
        return new ProfileRepositoryImpl(profileJpaRepository, profilePersistenceMapper);
    }
}
