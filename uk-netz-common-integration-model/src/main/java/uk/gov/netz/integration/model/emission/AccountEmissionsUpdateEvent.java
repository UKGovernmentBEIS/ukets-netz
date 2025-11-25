package uk.gov.netz.integration.model.emission;

import java.io.Serializable;
import java.time.Year;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEmissionsUpdateEvent implements Serializable {

    private Long registryId;
    private Long reportableEmissions;
    private Year reportingYear;
}
