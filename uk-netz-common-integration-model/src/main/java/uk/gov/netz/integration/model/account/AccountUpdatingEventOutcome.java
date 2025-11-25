package uk.gov.netz.integration.model.account;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import uk.gov.netz.integration.model.IntegrationEventOutcome;
import uk.gov.netz.integration.model.error.IntegrationEventErrorDetails;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatingEventOutcome {

    private AccountUpdatingEvent event;
    private String accountIdentifier;
    private List<IntegrationEventErrorDetails> errors;
    private IntegrationEventOutcome outcome;
}
