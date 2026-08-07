package uk.gov.netz.api.mireport.userdefined;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryEntity;
import uk.gov.netz.api.mireport.userdefined.category.MiReportUserDefinedCategoryMapper;

import java.util.Set;

@Mapper(componentModel = "spring", config = MapperConfig.class, uses = MiReportUserDefinedCategoryMapper.class)
public interface MiReportUserDefinedMapper {

    @Mapping(target = "categories", source = "categoryEntities")
    MiReportUserDefinedEntity toMiReportUserDefinedEntity(MiReportUserDefinedDTO miReportUserDefinedDTO,
          Set<MiReportUserDefinedCategoryEntity> categoryEntities,
			CompetentAuthorityEnum competentAuthority, String createdBy);

    @Mapping(target = "favourite", source = "favourite")
    MiReportUserDefinedDTO toMiReportUserDefinedDTO(MiReportUserDefinedEntity miReportUserDefinedEntity, boolean favourite);

    @Mapping(target = "categories", source = "categoryEntities")
    void updateMiReportUserDefinedEntity(
            @MappingTarget MiReportUserDefinedEntity entity,
            MiReportUserDefinedDTO dto,
            Set<MiReportUserDefinedCategoryEntity> categoryEntities);

    MiReportUserDefinedInfoDTO toMiReportUserDefinedInfoDTO(MiReportUserDefinedEntity entity);


}
