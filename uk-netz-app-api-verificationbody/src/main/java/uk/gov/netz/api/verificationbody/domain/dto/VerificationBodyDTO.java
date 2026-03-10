package uk.gov.netz.api.verificationbody.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.verificationbody.enumeration.VerificationBodyStatus;

import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VerificationBodyDTO {

    private Long id;
    private String name;
    private String accreditationReferenceNumber;
    private VerificationBodyStatus status;
    private AddressDTO address;

    @Builder.Default
    private Set<String> emissionTradingSchemes = new HashSet<>();
}
