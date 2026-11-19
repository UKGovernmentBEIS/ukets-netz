package uk.gov.netz.api.user.core.transform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationErrorDTO;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationResponseDTO;
import uk.gov.netz.api.user.core.domain.enumeration.PasswordPolicyErrorCodeEnum;
import uk.gov.netz.api.user.core.domain.model.keycloak.KeycloakPasswordValidationError;
import uk.gov.netz.api.user.core.domain.model.keycloak.KeycloakPasswordValidationResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PasswordValidityMapperTest {

    private PasswordValidityMapper mapper;

    @BeforeEach
    public void init() {
        mapper = Mappers.getMapper(PasswordValidityMapper.class);
    }

    @Test
    void toVerifierUserDTO() {
        List<KeycloakPasswordValidationError> messages = List.of(
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.WEAK, "message 1"),
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.WEAK, "message 2"),
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.INVALID_MAX_LENGTH, "message 3"),
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.PWNED_SERVICE_UNAVAILABLE, "message 4"),
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.OTHER, "message 5"),
            createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum.OTHER, "message 6"));

        KeycloakPasswordValidationResponse response = KeycloakPasswordValidationResponse.builder()
            .valid(true)
            .errors(messages)
            .build();

        List<PasswordValidationErrorDTO> messagesDTO = List.of(
            createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum.WEAK, "message 1"),
            createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum.INVALID_MAX_LENGTH, "message 3"),
            createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum.PWNED_SERVICE_UNAVAILABLE, "message 4"),
            createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum.OTHER, "message 5"),
            createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum.OTHER, "message 6")
        );

        PasswordValidationResponseDTO expected = PasswordValidationResponseDTO.builder()
            .valid(true)
            .errors(messagesDTO)
            .build();

        PasswordValidationResponseDTO actual = mapper.toPasswordValidationResponseDTO(response);

        assertEquals(expected, actual);
    }

    private KeycloakPasswordValidationError createKeycloakPasswordValidationError(PasswordPolicyErrorCodeEnum blacklisted,
                                                                                  String message) {
        return KeycloakPasswordValidationError.builder()
            .code(blacklisted)
            .message(message)
            .build();
    }

    private PasswordValidationErrorDTO createPasswordValidationErrorDTO(PasswordPolicyErrorCodeEnum blacklisted,
                                                                        String message) {
        return PasswordValidationErrorDTO.builder()
            .code(blacklisted)
            .message(message)
            .build();
    }
}