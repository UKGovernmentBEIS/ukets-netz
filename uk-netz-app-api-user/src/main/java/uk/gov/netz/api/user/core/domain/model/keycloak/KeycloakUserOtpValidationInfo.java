package uk.gov.netz.api.user.core.domain.model.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserOtpValidationInfo {

    private String otp;
    private String email;
}
