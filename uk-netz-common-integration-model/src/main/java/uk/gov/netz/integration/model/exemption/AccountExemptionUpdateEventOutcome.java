package uk.gov.netz.integration.model.exemption;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.integration.model.IntegrationEventOutcome;
import uk.gov.netz.integration.model.error.IntegrationEventErrorDetails;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountExemptionUpdateEventOutcome {

    private AccountExemptionUpdateEvent event;
    private List<IntegrationEventErrorDetails> errors;
    private IntegrationEventOutcome outcome;
}
