package uk.gov.netz.integration.model.withold;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.integration.model.IntegrationEventOutcome;
import uk.gov.netz.integration.model.error.IntegrationEventErrorDetails;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountWithholdUpdateEventOutcome {

    private AccountWithholdUpdateEvent event;
    private List<IntegrationEventErrorDetails> errors;
    private IntegrationEventOutcome outcome;
}
