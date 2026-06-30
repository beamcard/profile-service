package com.beamcard.profile.persistence.mapper;

import com.beamcard.profile.domain.model.Award;
import com.beamcard.profile.persistence.model.AwardJpa;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper
public interface AwardPersistenceMapper {

    Award toDomain(AwardJpa jpa);

    AwardJpa toJpa(Award award);

    List<Award> toDomain(List<AwardJpa> jpas);
}
