package uk.gov.netz.api.workflow.request.application.verificationbodyappointed;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.event.AccountVerificationBodyAppointedEvent;
import uk.gov.netz.api.account.service.AccountVerificationBodyNotificationService;

import java.util.Set;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class AccountVerificationBodyAppointedEventListenerTest {

    @InjectMocks
    private AccountVerificationBodyAppointedEventListener listener;
    
    @Mock
    private RequestVerificationBodyService requestVerificationBodyService;

    @Mock
    private AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    @Test
    void onAccountVerificationBodyAppointedEvent() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        AccountVerificationBodyAppointedEvent event = 
                AccountVerificationBodyAppointedEvent.builder().accountId(accountId).verificationBodyId(verificationBodyId).build();
        
        listener.onAccountVerificationBodyAppointedEvent(event);

        verify(requestVerificationBodyService, times(1)).appointVerificationBodyToRequestsOfAccount(verificationBodyId, accountId);
        verify(requestVerificationBodyService, times(1)).completeExistingNewVerificationBodySystemMessage(Set.of(accountId));
        verify(accountVerificationBodyNotificationService, times(1)).createNewVerificationBodySystemMessage(verificationBodyId, accountId);
        verifyNoMoreInteractions(requestVerificationBodyService, accountVerificationBodyNotificationService);
    }
}
