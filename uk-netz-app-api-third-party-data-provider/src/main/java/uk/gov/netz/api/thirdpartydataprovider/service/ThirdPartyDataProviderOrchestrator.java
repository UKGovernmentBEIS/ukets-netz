package uk.gov.netz.api.thirdpartydataprovider.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.thirdpartydataprovider.service.ThirdPartyDataProviderAuthorityService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.thirdpartydataprovider.auth.KeycloakClientCustomClient;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderCreateDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderSaveDTO;
import uk.gov.netz.api.thirdpartydataprovider.mapper.ThirdPartyDataProviderMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class ThirdPartyDataProviderOrchestrator {

	private final ThirdPartyDataProviderService thirdPartyDataProviderService;
	private final KeycloakClientCustomClient keycloakClientCustomClient;
	private final UserRoleTypeService userRoleTypeService;
	private final ThirdPartyDataProviderAuthorityService  thirdPartyDataProviderAuthorityService;
	private final ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;
	private final EntityManager entityManager;

	private static final ThirdPartyDataProviderMapper THIRD_PARTY_DATA_PROVIDER_MAPPER =
		Mappers.getMapper(ThirdPartyDataProviderMapper.class);

	@Transactional
	public void createThirdPartyDataProvider(AppUser appUser, ThirdPartyDataProviderCreateDTO dto) {
		validateThirdPartyDataProviderDoesNotExist(dto);

		ThirdPartyDataProviderClientCreateResponseDTO clientResponseDTO =
			keycloakClientCustomClient.createThirdPartyDataProviderClient(dto);

		saveThirdPartyDataProviderAndAuthorityOrRollbackOnError(appUser, clientResponseDTO);
	}

	private void saveThirdPartyDataProviderAndAuthorityOrRollbackOnError(AppUser appUser,
																		 ThirdPartyDataProviderClientCreateResponseDTO createResponse) {

		String clientEntityId = createResponse.getClientEntityId();
		try {
			String serviceAccountUserId = createResponse.getServiceAccountUserId();

			ThirdPartyDataProviderSaveDTO saveDTO = THIRD_PARTY_DATA_PROVIDER_MAPPER.mapToDTO(createResponse);

			Long thirdPartyDataProviderId = thirdPartyDataProviderService.create(saveDTO);

			thirdPartyDataProviderAuthorityService.createActiveAuthorityForRole(thirdPartyDataProviderId,
				serviceAccountUserId,
				appUser.getUserId());
			userRoleTypeService.createUserRoleTypeIfNotExist(
				serviceAccountUserId, RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER);
			entityManager.flush();
		} catch (Exception e) {
			log.error("Error when saving third party data provider", e);
			keycloakClientCustomClient.deleteThirdPartyDataProviderClient(clientEntityId);
			throw new BusinessException(ErrorCode.INTERNAL_SERVER);
		}
	}

	private void validateThirdPartyDataProviderDoesNotExist(ThirdPartyDataProviderCreateDTO dto) {
		if (thirdPartyDataProviderQueryService.existsByNameIgnoreCase(dto.getName())) {
			throw new BusinessException(ErrorCode.THIRD_PARTY_DATA_PROVIDER_NAME_EXISTS);
		}

		if (existsByJwksUrlIgnoreCase(dto.getJwksUrl())) {
			throw new BusinessException(ErrorCode.THIRD_PARTY_DATA_PROVIDER_JWKS_URL_EXISTS);
		}
	}

	private boolean existsByJwksUrlIgnoreCase(String jwksUrl) {
		List<ThirdPartyDataProviderClientResponseDTO> allClients =
			keycloakClientCustomClient.getAllThirdPartyDataProviderClients();

        return allClients.stream().anyMatch(client -> jwksUrl.equalsIgnoreCase(client.getJwksUrl()));
	}
}
