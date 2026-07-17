package com.beamcard.profile.persistence.mapper;

import com.beamcard.profile.domain.model.Profile;
import com.beamcard.profile.persistence.model.ProfileJpa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProfilePersistenceMapper {

    @Mapping(target = "location", ignore = true)
    @Mapping(target = "affiliations", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "priceItems", ignore = true)
    Profile toDomain(ProfileJpa jpa);

    ProfileJpa toJpa(Profile profile);
}
