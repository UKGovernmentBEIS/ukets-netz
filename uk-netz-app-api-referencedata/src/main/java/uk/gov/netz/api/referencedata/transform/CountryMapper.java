package uk.gov.netz.api.referencedata.transform;

import org.mapstruct.Mapper;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.referencedata.domain.Country;
import uk.gov.netz.api.referencedata.domain.dto.CountryDTO;

/**
 * The country mapper.
 */
@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface CountryMapper extends ReferenceDataMapper<Country, CountryDTO> {

}
