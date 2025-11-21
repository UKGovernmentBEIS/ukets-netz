package uk.gov.netz.api.common.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import uk.gov.netz.api.common.config.KeycloakProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakCustomClientUtilsProviderTest {

    @InjectMocks
    private KeycloakCustomClientUtilsProvider keycloakCustomClientUtilsProvider;

    @Mock
    private KeycloakProperties keycloakProperties;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Keycloak keycloakAdminClient;

    @Test
    void realmEndpointUrl() {
        when(keycloakProperties.getAuthServerUrl()).thenReturn("http://serverurl");
        when(keycloakProperties.getRealm()).thenReturn("uk-pmrv");

        String response = keycloakCustomClientUtilsProvider.realmEndpointUrl();

        assertEquals("http://serverurl/realms/uk-pmrv", response);

        verify(keycloakProperties).getAuthServerUrl();
        verify(keycloakProperties).getRealm();
        verifyNoMoreInteractions(keycloakProperties);
        verifyNoInteractions(keycloakAdminClient);
    }

    @Test
    void httpHeaders() {
        String token = "token";
        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.setBearerAuth(token);

        when(keycloakAdminClient.tokenManager().grantToken().getToken()).thenReturn(token);

        HttpHeaders response = keycloakCustomClientUtilsProvider.httpHeaders();

        verify(keycloakAdminClient.tokenManager().grantToken()).getToken();
        assertEquals(expectedHttpHeaders, response);
        verifyNoInteractions(keycloakProperties);
    }

    @Test
    void buildHttpHeadersWithAuthToken() {
        String token = "token";
        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.setBearerAuth(token);

        HttpHeaders response = keycloakCustomClientUtilsProvider.buildHttpHeadersWithAuthToken(token);

        assertEquals(expectedHttpHeaders, response);
        verifyNoInteractions(keycloakProperties, keycloakAdminClient);
    }
}