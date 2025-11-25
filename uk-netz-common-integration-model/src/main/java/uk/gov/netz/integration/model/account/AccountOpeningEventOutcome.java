package uk.gov.netz.integration.model.account;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.netz.integration.model.IntegrationEventOutcome;
import uk.gov.netz.integration.model.error.IntegrationEventErrorDetails;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountOpeningEventOutcome {

    private AccountOpeningEvent event;
    private String accountIdentifier;
    private List<IntegrationEventErrorDetails> errors;
    private IntegrationEventOutcome outcome;
}
