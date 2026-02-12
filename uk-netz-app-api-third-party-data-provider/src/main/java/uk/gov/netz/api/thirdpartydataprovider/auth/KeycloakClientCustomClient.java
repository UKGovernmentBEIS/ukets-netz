package uk.gov.netz.api.thirdpartydataprovider.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.netz.api.common.utils.KeycloakCustomClientUtilsProvider;
import uk.gov.netz.api.restclient.RestClientApi;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderCreateDTO;
import uk.gov.netz.api.thirdpartydataprovider.enumeration.KeycloakClientRestEndPointEnum;

import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class KeycloakClientCustomClient {

    private final RestTemplate restTemplate;
    private final KeycloakCustomClientUtilsProvider keycloakCustomClientUtilsProvider;

    public ThirdPartyDataProviderClientCreateResponseDTO createThirdPartyDataProviderClient(ThirdPartyDataProviderCreateDTO requestDTO) {

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(keycloakCustomClientUtilsProvider.realmEndpointUrl())
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_CREATE_THIRD_PARTY_DATA_PROVIDER.getPath())
                .build()
                .toUri())
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_CREATE_THIRD_PARTY_DATA_PROVIDER)
            .headers(keycloakCustomClientUtilsProvider.httpHeaders())
            .body(requestDTO)
            .restTemplate(restTemplate)
            .build();

        try {
            ResponseEntity<ThirdPartyDataProviderClientCreateResponseDTO> apiResponse = appRestApi.performApiCall();
            return apiResponse.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public ThirdPartyDataProviderClientResponseDTO getThirdPartyDataProviderClient(String clientEntityId) {

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(keycloakCustomClientUtilsProvider.realmEndpointUrl())
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_THIRD_PARTY_DATA_PROVIDER.getPath())
                .build(clientEntityId))
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_THIRD_PARTY_DATA_PROVIDER)
            .headers(keycloakCustomClientUtilsProvider.httpHeaders())
            .restTemplate(restTemplate)
            .build();

        try {
            ResponseEntity<ThirdPartyDataProviderClientResponseDTO> res = appRestApi.performApiCall();
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public List<ThirdPartyDataProviderClientResponseDTO> getAllThirdPartyDataProviderClients() {

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(keycloakCustomClientUtilsProvider.realmEndpointUrl())
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_ALL_THIRD_PARTY_DATA_PROVIDERS.getPath())
                .build()
                .toUri())
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_GET_ALL_THIRD_PARTY_DATA_PROVIDERS)
            .headers(keycloakCustomClientUtilsProvider.httpHeaders())
            .restTemplate(restTemplate)
            .build();

        try {
            ResponseEntity<List<ThirdPartyDataProviderClientResponseDTO>> res = appRestApi.performApiCall();
            return res.getBody();
        } catch (HttpClientErrorException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteThirdPartyDataProviderClient(String clientId) {

        RestClientApi appRestApi = RestClientApi.builder()
            .uri(UriComponentsBuilder
                .fromUriString(keycloakCustomClientUtilsProvider.realmEndpointUrl())
                .path(KeycloakClientRestEndPointEnum.KEYCLOAK_DELETE_THIRD_PARTY_DATA_PROVIDER.getPath())
                .pathSegment("{clientId}")
                .build(clientId))
            .restEndPoint(KeycloakClientRestEndPointEnum.KEYCLOAK_DELETE_THIRD_PARTY_DATA_PROVIDER)
            .headers(keycloakCustomClientUtilsProvider.httpHeaders())
            .restTemplate(restTemplate)
            .build();

        try {
            appRestApi.performApiCall();
        } catch (HttpClientErrorException e) {
            log.error("Failed to delete third-party data provider with clientId: {} with error: {}", clientId, e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
