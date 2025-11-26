package uk.gov.netz.api.thirdpartydataprovider.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.netz.api.restclient.RestClientEndPoint;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientCreateResponseDTO;
import uk.gov.netz.api.thirdpartydataprovider.domain.ThirdPartyDataProviderClientResponseDTO;

/**
 * The Keycloak client rest points enum.
 */
@Getter
@AllArgsConstructor
public enum KeycloakClientRestEndPointEnum implements RestClientEndPoint {

    /** Return clients registered in Keycloak. */
    KEYCLOAK_CREATE_THIRD_PARTY_DATA_PROVIDER("/third-party-data-provider-clients", HttpMethod.POST, new ParameterizedTypeReference<ThirdPartyDataProviderClientCreateResponseDTO>() {}),
    KEYCLOAK_DELETE_THIRD_PARTY_DATA_PROVIDER("/third-party-data-provider-clients", HttpMethod.DELETE, new ParameterizedTypeReference<Void>() {}),
    KEYCLOAK_GET_THIRD_PARTY_DATA_PROVIDER("/third-party-data-provider-clients/{client-entity-id}",HttpMethod.GET, new ParameterizedTypeReference<ThirdPartyDataProviderClientResponseDTO>() {})
    ;

    /** The url. */
    private final String path;

    /** The {@link HttpMethod}. */
    private final HttpMethod method;

    /** The {@link ParameterizedTypeReference}. */
    private final ParameterizedTypeReference<?> parameterizedTypeReference;
}
