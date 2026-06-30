package com.beamcard.profile.persistence.config;

import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.domain.repository.LinkRepository;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.AwardPersistenceMapper;
import com.beamcard.profile.persistence.mapper.LinkPersistenceMapper;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.repository.AwardRepositoryImpl;
import com.beamcard.profile.persistence.repository.LinkRepositoryImpl;
import com.beamcard.profile.persistence.repository.ProfileRepositoryImpl;
import com.beamcard.profile.persistence.repository.jpa.AffiliationJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.AwardJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.LinkJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import com.beamcard.profile.persistence.repository.jpa.ProfileLocationJpaRepository;
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
            ProfileJpaRepository profileJpaRepository,
            ProfileLocationJpaRepository profileLocationJpaRepository,
            AffiliationJpaRepository affiliationJpaRepository,
            ProfilePersistenceMapper profilePersistenceMapper) {
        return new ProfileRepositoryImpl(
                profileJpaRepository, profileLocationJpaRepository, affiliationJpaRepository, profilePersistenceMapper);
    }

    @Bean
    public LinkPersistenceMapper linkPersistenceMapper() {
        return Mappers.getMapper(LinkPersistenceMapper.class);
    }

    @Bean
    public LinkRepository linkRepository(
            LinkJpaRepository linkJpaRepository, LinkPersistenceMapper linkPersistenceMapper) {
        return new LinkRepositoryImpl(linkJpaRepository, linkPersistenceMapper);
    }

    @Bean
    public AwardPersistenceMapper awardPersistenceMapper() {
        return Mappers.getMapper(AwardPersistenceMapper.class);
    }

    @Bean
    public AwardRepository awardRepository(
            AwardJpaRepository awardJpaRepository, AwardPersistenceMapper awardPersistenceMapper) {
        return new AwardRepositoryImpl(awardJpaRepository, awardPersistenceMapper);
    }
}
