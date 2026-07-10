package uk.gov.netz.api.competentauthority;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;

@Mapper(
        componentModel = "spring",
        config = MapperConfig.class
)
public interface CompetentAuthorityMapper {

    CompetentAuthorityDTO toCompetentAuthorityDTO(CompetentAuthority competentAuthority);
}
