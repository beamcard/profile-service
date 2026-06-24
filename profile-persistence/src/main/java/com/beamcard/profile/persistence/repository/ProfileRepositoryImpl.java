package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.domain.repository.ProfileRepository;
import com.beamcard.profile.persistence.mapper.ProfilePersistenceMapper;
import com.beamcard.profile.persistence.repository.jpa.ProfileJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProfileRepositoryImpl implements ProfileRepository {

    private final ProfileJpaRepository jpaRepository;
    private final ProfilePersistenceMapper mapper;

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public Optional<Profile> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    public Profile save(Profile profile) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(profile)));
    }
}
