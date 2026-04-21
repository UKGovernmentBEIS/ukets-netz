package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.account.domain.event.AccountVerificationBodyAppointedEvent;
import uk.gov.netz.api.account.service.AccountVerificationBodyNotificationService;

import java.util.Set;

@RequiredArgsConstructor
@Component
public class AccountVerificationBodyAppointedEventListener {
    
    private final RequestVerificationBodyService requestVerificationBodyService;
    private final AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    //TODO: CHECK WHAT NEED TO BE DONE AND IN WHICH REQUESTS
    @EventListener
    public void onAccountVerificationBodyAppointedEvent(AccountVerificationBodyAppointedEvent event) {
        requestVerificationBodyService.appointVerificationBodyToRequestsOfAccount(event.getVerificationBodyId(), event.getAccountId());
        requestVerificationBodyService.completeExistingNewVerificationBodySystemMessage(Set.of(event.getAccountId()));
        accountVerificationBodyNotificationService.createNewVerificationBodySystemMessage(event.getVerificationBodyId(), event.getAccountId());
    }
}
