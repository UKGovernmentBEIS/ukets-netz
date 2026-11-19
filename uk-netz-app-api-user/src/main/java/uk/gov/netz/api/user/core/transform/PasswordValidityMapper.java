package uk.gov.netz.api.user.core.transform;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uk.gov.netz.api.common.config.MapperConfig;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationErrorDTO;
import uk.gov.netz.api.user.core.domain.dto.PasswordValidationResponseDTO;
import uk.gov.netz.api.user.core.domain.enumeration.PasswordPolicyErrorCodeEnum;
import uk.gov.netz.api.user.core.domain.model.keycloak.KeycloakPasswordValidationResponse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring", config = MapperConfig.class)
public interface PasswordValidityMapper {

    PasswordValidationResponseDTO toPasswordValidationResponseDTO(KeycloakPasswordValidationResponse response);

    @AfterMapping
    default void deduplicateErrors(@MappingTarget PasswordValidationResponseDTO responseDTO) {
        if (responseDTO.getErrors() == null || responseDTO.getErrors().isEmpty()) {
            return;
        }

        List<PasswordValidationErrorDTO> deduplicatedList = new ArrayList<>();
        Set<PasswordPolicyErrorCodeEnum> seenNonOtherCodes = new HashSet<>();

        for (PasswordValidationErrorDTO error : responseDTO.getErrors()) {
            PasswordPolicyErrorCodeEnum code = error.getCode();

            if (code == PasswordPolicyErrorCodeEnum.OTHER) {
                deduplicatedList.add(error);
            } else if (seenNonOtherCodes.add(code)) {
                deduplicatedList.add(error);
            }
        }

        responseDTO.setErrors(deduplicatedList);
    }
}
