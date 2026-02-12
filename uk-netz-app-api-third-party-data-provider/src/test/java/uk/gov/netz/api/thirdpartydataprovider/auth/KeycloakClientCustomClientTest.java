package uk.gov.netz.api.thirdpartydataprovider.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.utils.KeycloakCustomClientUtilsProvider;
import uk.gov.netz.api.restclient.RestClientApi;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderCreateDTO;
import uk.gov.netz.api.thirdpartydataprovider.enumeration.KeycloakClientRestEndPointEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@Import(ObjectMapper.class)
class KeycloakClientCustomClientTest {

    private static final String AUTH_SERVER_URL = "http://serverurl/realms/realm";

    @InjectMocks
    private KeycloakClientCustomClient client;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KeycloakCustomClientUtilsProvider keycloakCustomClientUtilsProvider;

    @Test
    void createThirdPartyDataProviderClient() {
        String name = "name";
        ThirdPartyDataProviderCreateDTO createDTO = ThirdPartyDataProviderCreateDTO.builder()
            .name(name)
            .jwksUrl("jwksUrl")
            .build();

        String token = "token";
        HttpHeaders httpHeaders = httpHeaders(token);

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(AUTH_SERVER_URL)
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_CREATE_THIRD_PARTY_DATA_PROVIDER.getPath())
                .build()
                .toUri())
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_CREATE_THIRD_PARTY_DATA_PROVIDER)
            .headers(httpHeaders)
            .body(createDTO)
            .restTemplate(restTemplate)
            .build();
        ThirdPartyDataProviderClientCreateResponseDTO expectedResponseDTO = ThirdPartyDataProviderClientCreateResponseDTO.builder()
            .clientEntityId("clientEntityId")
            .jwksUrl("jwksUrl")
            .serviceAccountUserId("serviceAccountUserId")
            .clientId("clientId")
            .build();

        when(keycloakCustomClientUtilsProvider.realmEndpointUrl()).thenReturn(AUTH_SERVER_URL);
        when(keycloakCustomClientUtilsProvider.httpHeaders()).thenReturn(httpHeaders);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.POST, new HttpEntity<>(createDTO, httpHeaders),
            new ParameterizedTypeReference<ThirdPartyDataProviderClientCreateResponseDTO>() {}))
            .thenReturn(new ResponseEntity<>(expectedResponseDTO, HttpStatus.OK));

        ThirdPartyDataProviderClientCreateResponseDTO actualResponseDTO = client.createThirdPartyDataProviderClient(createDTO);

        assertEquals(expectedResponseDTO, actualResponseDTO);

        verify(restTemplate, times(1)).exchange(appRestApi.getUri(), HttpMethod.POST, new HttpEntity<>(createDTO, httpHeaders),
            new ParameterizedTypeReference<ThirdPartyDataProviderClientCreateResponseDTO>() {});
        verify(keycloakCustomClientUtilsProvider).realmEndpointUrl();
        verify(keycloakCustomClientUtilsProvider).httpHeaders();

        verifyNoMoreInteractions(keycloakCustomClientUtilsProvider, restTemplate);
    }

    @Test
    void getThirdPartyDataProviderClient() {
        String clientEntityId = "clientEntityId";
        String token = "token";
        HttpHeaders httpHeaders = httpHeaders(token);

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(AUTH_SERVER_URL)
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_THIRD_PARTY_DATA_PROVIDER.getPath())
                .build(clientEntityId))
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_THIRD_PARTY_DATA_PROVIDER)
            .headers(httpHeaders)
            .restTemplate(restTemplate)
            .build();
        ThirdPartyDataProviderClientResponseDTO expectedResponseDTO = ThirdPartyDataProviderClientResponseDTO.builder()
            .jwksUrl("jwksUrl")
            .serviceAccountUserId("serviceAccountUserId")
            .clientId("clientId")
            .build();

        when(keycloakCustomClientUtilsProvider.realmEndpointUrl()).thenReturn(AUTH_SERVER_URL);
        when(keycloakCustomClientUtilsProvider.httpHeaders()).thenReturn(httpHeaders);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<ThirdPartyDataProviderClientResponseDTO>() {}))
            .thenReturn(new ResponseEntity<>(expectedResponseDTO, HttpStatus.OK));

        ThirdPartyDataProviderClientResponseDTO actualResponseDTO = client.getThirdPartyDataProviderClient(clientEntityId);

        assertEquals(expectedResponseDTO, actualResponseDTO);

        verify(restTemplate, times(1)).exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<ThirdPartyDataProviderClientResponseDTO>() {});
        verify(keycloakCustomClientUtilsProvider).realmEndpointUrl();
        verify(keycloakCustomClientUtilsProvider).httpHeaders();

        verifyNoMoreInteractions(keycloakCustomClientUtilsProvider, restTemplate);
    }

    @Test
    void getAllThirdPartyDataProviderClients() {
        String token = "token";
        HttpHeaders httpHeaders = httpHeaders(token);

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(AUTH_SERVER_URL)
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_ALL_THIRD_PARTY_DATA_PROVIDERS.getPath())
                .build().toUri())
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_ALL_THIRD_PARTY_DATA_PROVIDERS)
            .headers(httpHeaders)
            .restTemplate(restTemplate)
            .build();
        List<ThirdPartyDataProviderClientResponseDTO> expectedResponseDTO = List.of(
            ThirdPartyDataProviderClientResponseDTO.builder()
                .jwksUrl("jwksUrl")
                .serviceAccountUserId("serviceAccountUserId")
                .clientId("clientId")
                .build());

        when(keycloakCustomClientUtilsProvider.realmEndpointUrl()).thenReturn(AUTH_SERVER_URL);
        when(keycloakCustomClientUtilsProvider.httpHeaders()).thenReturn(httpHeaders);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<List<ThirdPartyDataProviderClientResponseDTO>>() {}))
            .thenReturn(new ResponseEntity<>(expectedResponseDTO, HttpStatus.OK));

        List<ThirdPartyDataProviderClientResponseDTO> actualResponseDTO = client.getAllThirdPartyDataProviderClients();

        assertEquals(expectedResponseDTO, actualResponseDTO);

        verify(restTemplate, times(1)).exchange(appRestApi.getUri(), HttpMethod.GET, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<List<ThirdPartyDataProviderClientResponseDTO>>() {});
        verify(keycloakCustomClientUtilsProvider).realmEndpointUrl();
        verify(keycloakCustomClientUtilsProvider).httpHeaders();

        verifyNoMoreInteractions(keycloakCustomClientUtilsProvider, restTemplate);
    }

    @Test
    void deleteThirdPartyDataProviderClient() {
        String clientId = "clientId";

        String token = "token";
        HttpHeaders httpHeaders = httpHeaders(token);

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(AUTH_SERVER_URL)
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_DELETE_THIRD_PARTY_DATA_PROVIDER.getPath())
                .pathSegment("{clientId}")
                .build(clientId))
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_DELETE_THIRD_PARTY_DATA_PROVIDER)
            .headers(httpHeaders)
            .restTemplate(restTemplate)
            .build();

        when(keycloakCustomClientUtilsProvider.realmEndpointUrl()).thenReturn(AUTH_SERVER_URL);
        when(keycloakCustomClientUtilsProvider.httpHeaders()).thenReturn(httpHeaders);
        when(restTemplate.exchange(appRestApi.getUri(), HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<Void>() {}))
            .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        client.deleteThirdPartyDataProviderClient(clientId);

        verify(restTemplate, times(1)).exchange(appRestApi.getUri(), HttpMethod.DELETE, new HttpEntity<>(httpHeaders),
            new ParameterizedTypeReference<Void>() {});
        verify(keycloakCustomClientUtilsProvider).realmEndpointUrl();
        verify(keycloakCustomClientUtilsProvider).httpHeaders();

        verifyNoMoreInteractions(keycloakCustomClientUtilsProvider, restTemplate);
    }

    private static HttpHeaders httpHeaders(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }
}