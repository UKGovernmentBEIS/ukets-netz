package uk.gov.netz.api.referencedata.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mapstruct.factory.Mappers;
import uk.gov.netz.api.referencedata.domain.ReferenceData;
import uk.gov.netz.api.referencedata.domain.dto.ReferenceDataDTO;
import uk.gov.netz.api.referencedata.domain.enumeration.ReferenceDataType;
import uk.gov.netz.api.referencedata.transform.CountryMapper;
import uk.gov.netz.api.referencedata.transform.CountyMapper;
import uk.gov.netz.api.referencedata.transform.ReferenceDataMapper;

import java.util.Arrays;

/**
 * Enum that relates a reference data type with a reference data service and a dto mapper
 *
 */
@AllArgsConstructor
@Getter
public enum ReferenceDataTypeServiceEnum {

    COUNTRY(ReferenceDataType.COUNTRIES, CountryService.class, Mappers.getMapper(CountryMapper.class)),
    COUNTY(ReferenceDataType.COUNTIES, CountyService.class, Mappers.getMapper(CountyMapper.class))
    //add more reference data types
    ;

    private final ReferenceDataType referenceDataType;
    private final Class<? extends ReferenceDataService<? extends ReferenceData>> referenceDataService;
    private final ReferenceDataMapper<? extends ReferenceData, ? extends ReferenceDataDTO> referenceDataMapper;

    public static ReferenceDataTypeServiceEnum resolve(ReferenceDataType referenceDataType) {
        return Arrays.stream(values())
                .filter(e -> e.getReferenceDataType() == referenceDataType)
                .findFirst()
                .orElse(null);
    }

}
