package uk.gov.netz.api.verificationbody.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.verificationbody.domain.dto.validation.StatusPending;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VerificationBodyUpdateStatusDTO {

    @NotNull
    private Long id;

    @NotNull
    @StatusPending
    private VerificationBodyStatus status;
}
