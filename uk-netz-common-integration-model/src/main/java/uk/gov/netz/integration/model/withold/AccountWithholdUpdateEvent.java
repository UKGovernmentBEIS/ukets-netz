package uk.gov.netz.integration.model.withold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountWithholdUpdateEvent {

    private Long registryId;
    private Boolean withholdFlag;
    private Year reportingYear;
}
