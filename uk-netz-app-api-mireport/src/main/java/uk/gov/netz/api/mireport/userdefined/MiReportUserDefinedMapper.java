package uk.gov.netz.api.mireport.userdefined;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface MiReportUserDefinedMapper {

	MiReportUserDefinedEntity toMiReportUserDefinedEntity(MiReportUserDefinedDTO miReportUserDefinedDTO,
			CompetentAuthorityEnum competentAuthority, String createdBy);

    MiReportUserDefinedDTO toMiReportUserDefinedDTO(MiReportUserDefinedEntity miReportUserDefinedEntity);

    void updateMiReportUserDefinedEntity(@MappingTarget MiReportUserDefinedEntity entity, MiReportUserDefinedDTO dto);
}
