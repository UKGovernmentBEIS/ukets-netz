package uk.gov.netz.integration.model.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdatingEvent {

    private UpdateAccountDetailsMessage accountDetails;
    private AccountHolderMessage accountHolder;
}
