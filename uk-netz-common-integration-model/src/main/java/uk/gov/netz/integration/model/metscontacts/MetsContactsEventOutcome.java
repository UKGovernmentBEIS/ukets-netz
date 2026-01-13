package uk.gov.netz.integration.model.metscontacts;

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
public class MetsContactsEventOutcome {

    private MetsContactsEvent event;
    private String accountIdentifier;
    private List<IntegrationEventErrorDetails> errors;
    private IntegrationEventOutcome outcome;
}
