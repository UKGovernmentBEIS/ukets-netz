package uk.gov.netz.api.thirdpartydataprovider.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.CompAuthAuthorizationResourceService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.thirdpartydataprovider.auth.KeycloakClientCustomClient;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderSaveDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvidersResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvider;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.mapper.ThirdPartyDataProviderMapper;
import uk.gov.netz.api.thirdpartydataprovider.repository.ThirdPartyDataProviderRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThirdPartyDataProviderService {

	private final ThirdPartyDataProviderRepository repo;
	private static final ThirdPartyDataProviderMapper MAPPER = Mappers.getMapper(ThirdPartyDataProviderMapper.class);
	private final CompAuthAuthorizationResourceService compAuthAuthorizationResourceService;
	private final KeycloakClientCustomClient keycloakClientCustomClient;

	@Transactional
	public Long create(ThirdPartyDataProviderSaveDTO data) {
		ThirdPartyDataProvider thirdPartyDataProvider = repo.save(MAPPER.mapToEntity(data));
		return thirdPartyDataProvider.getId();
	}

	@Transactional
	public void update(Long id, ThirdPartyDataProviderDTO saveDto) {
		ThirdPartyDataProvider entity = repo.findById(id)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
		MAPPER.update(entity, saveDto);
		repo.save(entity);
	}

	@Transactional(readOnly = true)
	public ThirdPartyDataProvidersResponseDTO getAllThirdPartyDataProviders(AppUser appUser) {
		boolean editable = compAuthAuthorizationResourceService
			.hasUserScopeToCompAuth(appUser, Scope.MANAGE_THIRD_PARTY_DATA_PROVIDERS);

		List<ThirdPartyDataProviderDTO> thirdPartyDataProviders = repo.findAll()
			.stream()
			.map(provider -> MAPPER.mapToDTO(provider).withJwksUrl(
				keycloakClientCustomClient.getThirdPartyDataProviderClient(provider.getClientEntityId()).getJwksUrl())
			)
			.toList();

		return ThirdPartyDataProvidersResponseDTO.builder()
			.editable(editable)
			.thirdPartyDataProviders(thirdPartyDataProviders)
			.build();
	}

	@Transactional(readOnly = true)
	public ThirdPartyDataProviderNameInfoDTO getThirdPartyDataProviderNameInfoById(Long thirdPartyDataProviderId) {
		ThirdPartyDataProvider dataProvider = repo.findById(thirdPartyDataProviderId)
			.orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
		return MAPPER.toThirdPartyDataProviderNameInfoDTO(dataProvider);
	}
}
