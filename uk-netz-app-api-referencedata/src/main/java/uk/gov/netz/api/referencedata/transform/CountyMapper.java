package uk.gov.netz.api.referencedata.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.referencedata.domain.County;
import uk.gov.netz.api.referencedata.domain.dto.CountyDTO;

/**
 * The county mapper.
 */
@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface CountyMapper extends ReferenceDataMapper<County, CountyDTO> {

}
