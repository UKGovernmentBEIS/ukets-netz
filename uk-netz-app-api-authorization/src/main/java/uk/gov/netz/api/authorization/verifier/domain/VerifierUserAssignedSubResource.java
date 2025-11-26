package uk.gov.netz.api.authorization.verifier.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifierUserAssignedSubResource {
    private Long verificationBodyId;
    private String resourceSubType;
}
