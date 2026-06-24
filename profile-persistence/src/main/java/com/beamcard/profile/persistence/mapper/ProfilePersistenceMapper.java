package com.beamcard.profile.persistence.mapper;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.persistence.model.ProfileJpa;
import org.mapstruct.Mapper;

@Mapper
public interface ProfilePersistenceMapper {

    Profile toDomain(ProfileJpa jpa);

    ProfileJpa toJpa(Profile profile);
}
