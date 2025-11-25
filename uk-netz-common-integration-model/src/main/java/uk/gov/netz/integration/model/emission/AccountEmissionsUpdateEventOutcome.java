package uk.gov.netz.integration.model.emission;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.integration.model.IntegrationEventOutcome;
import uk.gov.netz.integration.model.error.IntegrationEventError;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEmissionsUpdateEventOutcome {

    private AccountEmissionsUpdateEvent event;
    private List<IntegrationEventError> errors;
    private IntegrationEventOutcome outcome;
}
