package uk.gov.netz.api.thirdpartydataprovider.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ThirdPartyDataProviderOrchestratorTest {

    @InjectMocks
    private ThirdPartyDataProviderOrchestrator orchestrator;

    @Mock
    private ThirdPartyDataProviderService thirdPartyDataProviderService;
    @Mock
    private KeycloakClientCustomClient keycloakClientCustomClient;
    @Mock
    private UserRoleTypeService userRoleTypeService;
    @Mock
    private ThirdPartyDataProviderAuthorityService thirdPartyDataProviderAuthorityService;
    @Mock
    private ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;
    @Mock
    private EntityManager entityManager;

    @Test
    void createThirdPartyDataProvider() {
        String name = "name";
        String clientId = "clientId";
        String jwksUrl = "jwksUrl";
        String serviceAccountUserId = "serviceAccountUserId";
        String clientEntityId = "clientEntityId";
        long thirdPartyDataProviderId = 1L;

        AppUser appUser = AppUser.builder().userId("user-id").build();
        ThirdPartyDataProviderCreateDTO createDTO = ThirdPartyDataProviderCreateDTO.builder()
            .jwksUrl(jwksUrl)
            .name(name)
            .build();
        ThirdPartyDataProviderClientCreateResponseDTO keycloakResponseDTO = ThirdPartyDataProviderClientCreateResponseDTO.builder()
            .clientId(clientId)
            .clientEntityId(clientEntityId)
            .serviceAccountUserId(serviceAccountUserId)
            .jwksUrl(jwksUrl)
            .name(name)
            .build();
        ThirdPartyDataProviderSaveDTO saveDTO = ThirdPartyDataProviderSaveDTO.builder()
            .clientId(clientId)
            .name(name)
            .clientEntityId(clientEntityId)
            .build();

        when(thirdPartyDataProviderQueryService.existsByNameIgnoreCase(name)).thenReturn(false);
        when(keycloakClientCustomClient.getAllThirdPartyDataProviderClients())
            .thenReturn(List.of(ThirdPartyDataProviderClientResponseDTO.builder().jwksUrl("notjwksUrl").build()));
        when(keycloakClientCustomClient.createThirdPartyDataProviderClient(createDTO)).thenReturn(keycloakResponseDTO);
        when(thirdPartyDataProviderService.create(saveDTO)).thenReturn(thirdPartyDataProviderId);

        orchestrator.createThirdPartyDataProvider(appUser, createDTO);

        verify(thirdPartyDataProviderAuthorityService)
            .createActiveAuthorityForRole(thirdPartyDataProviderId, serviceAccountUserId, appUser.getUserId());
        verify(userRoleTypeService)
            .createUserRoleTypeIfNotExist(serviceAccountUserId, RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER);
        verify(thirdPartyDataProviderQueryService).existsByNameIgnoreCase(name);
        verify(keycloakClientCustomClient).getAllThirdPartyDataProviderClients();
        verify(keycloakClientCustomClient).createThirdPartyDataProviderClient(createDTO);
        verify(thirdPartyDataProviderService).create(saveDTO);
        verify(entityManager).flush();

        verifyNoMoreInteractions(thirdPartyDataProviderService, keycloakClientCustomClient, userRoleTypeService,
            thirdPartyDataProviderAuthorityService, thirdPartyDataProviderQueryService, entityManager);
    }

    @Test
    void createThirdPartyDataProvider_throws_exception_on_flush() {
        String clientEntityId = "clientEntityId";
        String name = "name";
        String clientId = "clientId";
        String jwksUrl = "jwksUrl";
        String serviceAccountUserId = "serviceAccountUserId";
        long thirdPartyDataProviderId = 1L;

        AppUser appUser = AppUser.builder().userId("user-id").build();
        ThirdPartyDataProviderCreateDTO createDTO = ThirdPartyDataProviderCreateDTO.builder()
            .name(name)
            .jwksUrl(jwksUrl)
            .build();
        ThirdPartyDataProviderClientCreateResponseDTO keycloakResponseDTO = ThirdPartyDataProviderClientCreateResponseDTO.builder()
            .clientId(clientId)
            .clientEntityId(clientEntityId)
            .serviceAccountUserId(serviceAccountUserId)
            .jwksUrl(jwksUrl)
            .name(name)
            .build();
        ThirdPartyDataProviderSaveDTO saveDTO = ThirdPartyDataProviderSaveDTO.builder()
            .clientId(clientId)
            .name(name)
            .clientEntityId(clientEntityId)
            .build();

        Exception error = new RuntimeException("Error upon flush");

        when(thirdPartyDataProviderQueryService.existsByNameIgnoreCase(name)).thenReturn(false);
        when(keycloakClientCustomClient.getAllThirdPartyDataProviderClients())
            .thenReturn(List.of(ThirdPartyDataProviderClientResponseDTO.builder().jwksUrl("notjwksUrl").build()));
        when(keycloakClientCustomClient.createThirdPartyDataProviderClient(createDTO)).thenReturn(keycloakResponseDTO);
        when(thirdPartyDataProviderService.create(saveDTO)).thenReturn(thirdPartyDataProviderId);
        doThrow(error).when(entityManager).flush();

        BusinessException exc = assertThrows(BusinessException.class, () ->
            orchestrator.createThirdPartyDataProvider(appUser, createDTO));

        assertEquals(ErrorCode.INTERNAL_SERVER, exc.getErrorCode());


        verify(thirdPartyDataProviderAuthorityService)
            .createActiveAuthorityForRole(thirdPartyDataProviderId, serviceAccountUserId, appUser.getUserId());
        verify(userRoleTypeService)
            .createUserRoleTypeIfNotExist(serviceAccountUserId, RoleTypeConstants.THIRD_PARTY_DATA_PROVIDER);
        verify(thirdPartyDataProviderQueryService).existsByNameIgnoreCase(name);
        verify(keycloakClientCustomClient).getAllThirdPartyDataProviderClients();
        verify(keycloakClientCustomClient).createThirdPartyDataProviderClient(createDTO);
        verify(thirdPartyDataProviderService).create(saveDTO);
        verify(entityManager).flush();
        verify(keycloakClientCustomClient).deleteThirdPartyDataProviderClient(clientEntityId);

        verifyNoMoreInteractions(thirdPartyDataProviderService, keycloakClientCustomClient, userRoleTypeService,
            thirdPartyDataProviderAuthorityService, thirdPartyDataProviderQueryService, entityManager);
    }

    @Test
    void createThirdPartyDataProvider_throws_exception_THIRD_PARTY_DATA_PROVIDER_NAME_EXISTS() {
        String name = "name";
        String jwksUrl = "jwksUrl";

        AppUser appUser = AppUser.builder().userId("user-id").build();
        ThirdPartyDataProviderCreateDTO createDTO = ThirdPartyDataProviderCreateDTO.builder()
            .name(name)
            .jwksUrl(jwksUrl)
            .build();

        when(thirdPartyDataProviderQueryService.existsByNameIgnoreCase(name)).thenReturn(true);

        BusinessException businessException = assertThrows(BusinessException.class, () ->
            orchestrator.createThirdPartyDataProvider(appUser, createDTO));

        assertEquals(ErrorCode.THIRD_PARTY_DATA_PROVIDER_NAME_EXISTS, businessException.getErrorCode());

        verify(thirdPartyDataProviderQueryService).existsByNameIgnoreCase(name);

        verifyNoMoreInteractions(thirdPartyDataProviderQueryService);
        verifyNoInteractions(thirdPartyDataProviderService, keycloakClientCustomClient, userRoleTypeService,
            thirdPartyDataProviderAuthorityService, entityManager);
    }


    @ParameterizedTest
    @MethodSource("createThirdPartyDataProviderJwksUrlExistsScenarios")
    void createThirdPartyDataProvider_throws_exception_THIRD_PARTY_DATA_PROVIDER_JWKS_URL_EXISTS(String  dtoJwksUrl,
                                                                                                 String  keycloakJwksUrl) {
        String name = "name";

        AppUser appUser = AppUser.builder().userId("user-id").build();
        ThirdPartyDataProviderCreateDTO createDTO = ThirdPartyDataProviderCreateDTO.builder()
            .name(name)
            .jwksUrl(dtoJwksUrl)
            .build();

        when(keycloakClientCustomClient.getAllThirdPartyDataProviderClients())
            .thenReturn(List.of(ThirdPartyDataProviderClientResponseDTO.builder().jwksUrl(keycloakJwksUrl).build()));
        when(thirdPartyDataProviderQueryService.existsByNameIgnoreCase(name)).thenReturn(false);
        BusinessException businessException = assertThrows(BusinessException.class, () ->
            orchestrator.createThirdPartyDataProvider(appUser, createDTO));

        assertEquals(ErrorCode.THIRD_PARTY_DATA_PROVIDER_JWKS_URL_EXISTS, businessException.getErrorCode());

        verify(keycloakClientCustomClient).getAllThirdPartyDataProviderClients();
        verify(thirdPartyDataProviderQueryService).existsByNameIgnoreCase(name);

        verifyNoMoreInteractions(thirdPartyDataProviderQueryService, keycloakClientCustomClient);
        verifyNoInteractions(thirdPartyDataProviderService, userRoleTypeService,
            thirdPartyDataProviderAuthorityService, entityManager);
    }

    private static Stream<Arguments> createThirdPartyDataProviderJwksUrlExistsScenarios() {
        return Stream.of(
            Arguments.of("jwksurl", "JWKSURL"),
            Arguments.of("JwksUrl", "jWksurl"),
            Arguments.of("jwksUrl", "jwksUrl")
        );
    }
}