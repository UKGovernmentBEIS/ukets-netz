package uk.gov.netz.api.verificationbody.domain.verificationbodydetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.verificationbody.domain.dto.AddressDTO;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerificationBodyDetails {

    private String name;
    private String accreditationReferenceNumber;
    private AddressDTO address;

    @Builder.Default
    private Set<String> emissionTradingSchemes = new HashSet<>();
}
