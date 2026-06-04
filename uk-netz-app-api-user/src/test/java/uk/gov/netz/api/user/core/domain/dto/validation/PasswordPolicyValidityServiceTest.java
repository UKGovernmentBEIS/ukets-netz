package uk.gov.netz.api.user.core.domain.dto.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationRequestDTO;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationResponseDTO;
import uk.gov.netz.api.user.core.domain.model.keycloak.KeycloakPasswordValidationResponse;
import uk.gov.netz.api.user.core.service.auth.KeycloakValidatePasswordCustomClient;
import uk.gov.netz.api.user.core.transform.PasswordValidityMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordPolicyValidityServiceTest {

    @InjectMocks
    private PasswordPolicyValidityService service;

    @Mock
    private KeycloakValidatePasswordCustomClient keycloakCustomClient;
    @Mock
    private PasswordValidityMapper passwordValidityMapper;

    @Test
    void validate() {
        String password = "password";
        PasswordValidationRequestDTO requestDTO = PasswordValidationRequestDTO.builder().password(password).build();
        KeycloakPasswordValidationResponse keycloakResponse = mock(KeycloakPasswordValidationResponse.class);
        PasswordValidationResponseDTO passwordValidationResponseDTO = mock(PasswordValidationResponseDTO.class);
        when(keycloakCustomClient.validatePassword(password)).thenReturn(keycloakResponse);
        when(passwordValidityMapper.toPasswordValidationResponseDTO(keycloakResponse)).thenReturn(passwordValidationResponseDTO);

        PasswordValidationResponseDTO actual = service.validate(requestDTO);

        assertEquals(passwordValidationResponseDTO, actual);

        verify(keycloakCustomClient).validatePassword(password);
        verify(passwordValidityMapper).toPasswordValidationResponseDTO(keycloakResponse);
        verifyNoMoreInteractions(keycloakCustomClient, passwordValidityMapper);
    }
}