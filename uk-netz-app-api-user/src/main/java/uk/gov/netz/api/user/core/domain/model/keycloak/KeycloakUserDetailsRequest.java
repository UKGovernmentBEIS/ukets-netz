package uk.gov.netz.api.user.core.domain.model.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.user.core.domain.model.core.SignatureRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeycloakUserDetailsRequest {
    
    private String id;
    private SignatureRequest signature;
}
