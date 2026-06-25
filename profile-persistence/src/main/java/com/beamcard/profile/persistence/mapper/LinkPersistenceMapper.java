package com.beamcard.profile.persistence.mapper;

import com.beamcard.profile.domain.model.Link;
import com.beamcard.profile.persistence.model.LinkJpa;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface LinkPersistenceMapper {

    Link toDomain(LinkJpa jpa);

    LinkJpa toJpa(Link link);

    List<Link> toDomain(List<LinkJpa> jpas);
}
