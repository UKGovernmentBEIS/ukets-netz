package uk.gov.netz.api.common.utils;

import lombok.AllArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.common.config.KeycloakProperties;

@AllArgsConstructor
@Component
public class KeycloakCustomClientUtilsProvider {

    private final KeycloakProperties keycloakProperties;
    private final Keycloak keycloakAdminClient;

    public String realmEndpointUrl() {
        return keycloakProperties.getAuthServerUrl()
            .concat("/realms/")
            .concat(keycloakProperties.getRealm());
    }

    public HttpHeaders httpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(keycloakAdminClient.tokenManager().grantToken().getToken());
        return httpHeaders;
    }

    public HttpHeaders buildHttpHeadersWithAuthToken(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(token);
        return httpHeaders;
    }
}
