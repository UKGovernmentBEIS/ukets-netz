package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.account.domain.event.AccountsVerificationBodyUnappointedEvent;

@RequiredArgsConstructor
@Component
public class AccountsVerificationBodyUnappointedEventListener {

    private final RequestVerificationBodyService requestVerificationBodyService;

    @EventListener
    public void onAccountsVerificationBodyUnappointedEvent(AccountsVerificationBodyUnappointedEvent event) {
        requestVerificationBodyService.unappointVerificationBodyFromRequestsOfAccounts(event.getAccountIds());
    }
}
