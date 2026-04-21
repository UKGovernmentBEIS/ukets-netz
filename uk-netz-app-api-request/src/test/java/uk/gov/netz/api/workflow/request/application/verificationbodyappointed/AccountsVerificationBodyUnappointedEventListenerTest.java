package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.event.AccountsVerificationBodyUnappointedEvent;
import uk.gov.netz.api.account.service.AccountVerificationBodyNotificationService;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AccountsVerificationBodyUnappointedEventListenerTest {

    @InjectMocks
    private AccountsVerificationBodyUnappointedEventListener listener;

    @Mock
    private RequestVerificationBodyService requestVerificationBodyService;

    @Mock
    private AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    @Test
    void onAccountsVerificationBodyUnappointedEvent() {
        Set<Long> accountIds = Set.of(1L, 2L);
        AccountsVerificationBodyUnappointedEvent event =
            AccountsVerificationBodyUnappointedEvent.builder().accountIds(accountIds).build();

        listener.onAccountsVerificationBodyUnappointedEvent(event);

        verify(requestVerificationBodyService, times(1)).unappointVerificationBodyFromRequestsOfAccounts(accountIds);
        verify(requestVerificationBodyService, times(1)).completeExistingNewVerificationBodySystemMessage(accountIds);
        verify(accountVerificationBodyNotificationService, times(1)).createVerificationBodyNoLongerAvailableSystemMessage(accountIds);
        verifyNoMoreInteractions(requestVerificationBodyService, accountVerificationBodyNotificationService);
    }
}