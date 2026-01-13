package uk.gov.netz.integration.model.exemption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Year;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountExemptionUpdateEvent {

    private Long registryId;
    private Boolean exemptionFlag;
    private Year reportingYear;

}
