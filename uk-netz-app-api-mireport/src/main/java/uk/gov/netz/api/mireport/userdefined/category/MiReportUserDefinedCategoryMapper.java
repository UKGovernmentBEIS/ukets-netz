package uk.gov.netz.api.mireport.userdefined.category;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;


@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface MiReportUserDefinedCategoryMapper {

    MiReportUserDefinedCategoryDTO toMiReportUserDefinedCategoryDTO(MiReportUserDefinedCategoryEntity entity);

}
