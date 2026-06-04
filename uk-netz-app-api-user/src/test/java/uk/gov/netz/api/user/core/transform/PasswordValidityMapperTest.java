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
            KeycloakPasswordValidationError.builder()
                .code(PasswordPolicyErrorCodeEnum.PWNED)
                .message("message")
                .build());

        KeycloakPasswordValidationResponse response = KeycloakPasswordValidationResponse.builder()
            .valid(true)
            .errors(messages)
            .build();

        List<PasswordValidationErrorDTO> messagesDTO = List.of(
            PasswordValidationErrorDTO.builder()
                .code(PasswordPolicyErrorCodeEnum.PWNED)
                .message("message")
                .build());

        PasswordValidationResponseDTO expected = PasswordValidationResponseDTO.builder()
            .valid(true)
            .errors(messagesDTO)
            .build();

        PasswordValidationResponseDTO actual = mapper.toPasswordValidationResponseDTO(response);

        assertEquals(expected, actual);
    }
}