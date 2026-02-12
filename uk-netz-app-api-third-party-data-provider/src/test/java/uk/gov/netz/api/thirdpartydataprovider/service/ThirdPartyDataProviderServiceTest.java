package uk.gov.netz.api.thirdpartydataprovider.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.CompAuthAuthorizationResourceService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.thirdpartydataprovider.auth.KeycloakClientCustomClient;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvider;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderNameInfoDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderSaveDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProvidersResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.repository.ThirdPartyDataProviderRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDataProviderServiceTest {
	
	@InjectMocks
    private ThirdPartyDataProviderService cut;
	
	@Mock
    private ThirdPartyDataProviderRepository repo;

	@Mock
	private CompAuthAuthorizationResourceService compAuthAuthorizationResourceService;

	@Mock
	private KeycloakClientCustomClient keycloakClientCustomClient;

	@Test
	void create() {
		ThirdPartyDataProviderSaveDTO dto = ThirdPartyDataProviderSaveDTO.builder()
				.clientId("clientId")
				.name("clientname")
				.clientEntityId("clientEntityId")
				.build();

		ThirdPartyDataProvider thirdPartyDataProvider = ThirdPartyDataProvider.builder()
			.clientId("clientId")
			.name("clientname")
			.clientEntityId("clientEntityId")
			.build();

		long expectedId = 1L;
		ThirdPartyDataProvider thirdPartyDataProviderEntity = ThirdPartyDataProvider.builder()
			.id(expectedId)
			.clientEntityId("clientEntityId")
			.build();

		when(repo.save(thirdPartyDataProvider)).thenReturn(thirdPartyDataProviderEntity);

		long actualId = cut.create(dto);

		assertEquals(expectedId, actualId);
		verify(repo, times(1)).save(ThirdPartyDataProvider.builder()
				.clientId("clientId")
				.name("clientname")
				.clientEntityId("clientEntityId")
				.build());
		verifyNoMoreInteractions(repo);
		verifyNoInteractions(compAuthAuthorizationResourceService);
	}
	
	@Test
	void update() {
		Long id = 1L;
		ThirdPartyDataProviderDTO dto = ThirdPartyDataProviderDTO.builder()
				.clientId("clientId2")
				.name("name2")
				.build();
		
		when(repo.findById(id)).thenReturn(Optional.of(ThirdPartyDataProvider.builder()
				.id(id)
				.clientId("clientId")
				.name("name")
				.build()));
		
		cut.update(id, dto);
		
		verify(repo, times(1)).save(ThirdPartyDataProvider.builder()
				.id(id)
				.clientId("clientId2")
				.name("name2")
				.build());
		verifyNoMoreInteractions(repo);
		verifyNoInteractions(compAuthAuthorizationResourceService);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void getAllThirdPartyDataProviders(boolean isEditable) {
		ThirdPartyDataProvider thirdPartyDataProvider = ThirdPartyDataProvider.builder()
			.id(1L)
			.clientId("clientId")
			.name("clientname")
			.clientEntityId("clientEntityId")
			.build();

		AppUser appUser = mock(AppUser.class);

		when(repo.findAll()).thenReturn(List.of(thirdPartyDataProvider));
		when(compAuthAuthorizationResourceService.hasUserScopeToCompAuth(appUser, Scope.MANAGE_THIRD_PARTY_DATA_PROVIDERS))
			.thenReturn(isEditable);
		when(keycloakClientCustomClient.getThirdPartyDataProviderClient("clientEntityId"))
			.thenReturn(ThirdPartyDataProviderClientResponseDTO.builder().jwksUrl("jwksUrl").build());

		ThirdPartyDataProvidersResponseDTO response = cut.getAllThirdPartyDataProviders(appUser);

		assertEquals(1, response.getThirdPartyDataProviders().size());
		assertEquals(ThirdPartyDataProviderDTO.builder()
			.id(1L)
			.clientId("clientId")
			.jwksUrl("jwksUrl")
			.name("clientname")
			.build(), response.getThirdPartyDataProviders().getFirst());
		assertEquals(isEditable, response.isEditable());

		verify(compAuthAuthorizationResourceService)
			.hasUserScopeToCompAuth(appUser, Scope.MANAGE_THIRD_PARTY_DATA_PROVIDERS);
		verify(keycloakClientCustomClient).getThirdPartyDataProviderClient("clientEntityId");
		verify(repo).findAll();
		verifyNoMoreInteractions(repo, compAuthAuthorizationResourceService, keycloakClientCustomClient);
	}

	@Test
	void getThirdPartyDataProviderNameInfoById() {
		Long thirdPartyDataProviderId = 1L;
		ThirdPartyDataProvider thirdPartyDataProvider = ThirdPartyDataProvider.builder()
			.id(thirdPartyDataProviderId)
			.clientId("clientId")
			.name("clientname")
			.build();

		when(repo.findById(thirdPartyDataProviderId)).thenReturn(Optional.ofNullable(thirdPartyDataProvider));

		ThirdPartyDataProviderNameInfoDTO response = cut.getThirdPartyDataProviderNameInfoById(1L);

		assertEquals(ThirdPartyDataProviderNameInfoDTO.builder()
			.id(thirdPartyDataProviderId)
			.name("clientname")
			.build(), response);

		verify(repo).findById(thirdPartyDataProviderId);
		verifyNoMoreInteractions(repo);
		verifyNoInteractions(compAuthAuthorizationResourceService);
	}


	@Test
	void getThirdPartyDataProviderNameInfoById_throws_resource_not_found() {
		Long thirdPartyDataProviderId = 1L;

		when(repo.findById(thirdPartyDataProviderId)).thenReturn(Optional.empty());

		BusinessException businessException = assertThrows(BusinessException.class, () ->
			cut.getThirdPartyDataProviderNameInfoById(1L));

		assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

		verify(repo).findById(thirdPartyDataProviderId);
		verifyNoMoreInteractions(repo);
		verifyNoInteractions(compAuthAuthorizationResourceService);
	}
}
