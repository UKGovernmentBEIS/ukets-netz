package uk.gov.netz.api.thirdpartydataprovider.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvider;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderSaveDTO;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface ThirdPartyDataProviderMapper {

	ThirdPartyDataProvider mapToEntity(ThirdPartyDataProviderSaveDTO dto);

	ThirdPartyDataProviderDTO mapToDTO(ThirdPartyDataProvider entity);

	void update(@MappingTarget ThirdPartyDataProvider entity, ThirdPartyDataProviderDTO dto);

	ThirdPartyDataProviderSaveDTO mapToDTO(ThirdPartyDataProviderClientCreateResponseDTO dto);

	ThirdPartyDataProviderNameInfoDTO toThirdPartyDataProviderNameInfoDTO(ThirdPartyDataProvider entity);
}
