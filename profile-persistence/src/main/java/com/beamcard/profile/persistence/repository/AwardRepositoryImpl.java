package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.domain.repository.AwardRepository;
import com.beamcard.profile.persistence.mapper.AwardPersistenceMapper;
import com.beamcard.profile.persistence.repository.jpa.AwardJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AwardRepositoryImpl implements AwardRepository {

    private final AwardJpaRepository jpaRepository;
    private final AwardPersistenceMapper mapper;

    @Override
    public List<Award> findByProfileId(UUID profileId) {
        return mapper.toDomain(jpaRepository.findByProfileIdOrderByPositionAsc(profileId));
    }

    @Override
    public Optional<Award> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Award save(Award award) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(award)));
    }

    @Override
    public List<Award> saveAll(List<Award> awards) {
        return mapper.toDomain(
                jpaRepository.saveAll(awards.stream().map(mapper::toJpa).toList()));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
