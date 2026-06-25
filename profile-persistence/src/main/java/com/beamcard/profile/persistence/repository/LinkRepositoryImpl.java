package com.beamcard.profile.persistence.repository;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.domain.repository.LinkRepository;
import com.beamcard.profile.persistence.mapper.LinkPersistenceMapper;
import com.beamcard.profile.persistence.repository.jpa.LinkJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LinkRepositoryImpl implements LinkRepository {

    private final LinkJpaRepository jpaRepository;
    private final LinkPersistenceMapper mapper;

    @Override
    public List<Link> findByProfileId(UUID profileId) {
        return mapper.toDomain(jpaRepository.findByProfileIdOrderByPositionAsc(profileId));
    }

    @Override
    public Optional<Link> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Link save(Link link) {
        return mapper.toDomain(jpaRepository.save(mapper.toJpa(link)));
    }

    @Override
    public List<Link> saveAll(List<Link> links) {
        return mapper.toDomain(
                jpaRepository.saveAll(links.stream().map(mapper::toJpa).toList()));
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }
}
