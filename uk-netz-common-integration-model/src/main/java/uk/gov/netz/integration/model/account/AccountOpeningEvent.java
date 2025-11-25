package uk.gov.netz.integration.model.account;

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
public class AccountOpeningEvent {

    private AccountType accountType;
    private AccountDetailsMessage accountDetails;
    private AccountHolderMessage accountHolder;
}
