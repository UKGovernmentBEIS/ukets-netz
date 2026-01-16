package uk.gov.netz.integration.model.exemption;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountExemptionUpdateEvent {

    private Long registryId;
    private Boolean exemptionFlag;
    private Year reportingYear;

}
