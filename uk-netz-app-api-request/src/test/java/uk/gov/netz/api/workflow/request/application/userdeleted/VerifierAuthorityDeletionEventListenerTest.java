package uk.gov.netz.api.workflow.request.application.userdeleted;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.verifier.event.VerifierAuthorityDeletionEvent;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.verifier.VerifierRequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service.SystemMessageNotificationRequestService;

@ExtendWith(MockitoExtension.class)
class VerifierAuthorityDeletionEventListenerTest {

    @InjectMocks
    private VerifierAuthorityDeletionEventListener listener;

    @Mock
    private VerifierRequestTaskAssignmentService verifierRequestTaskAssignmentService;

    @Mock
    private SystemMessageNotificationRequestService systemMessageNotificationRequestService;

    @Test
    void onVerifierUserDeletedEvent() {
        final String userId = "user";
        VerifierAuthorityDeletionEvent event = VerifierAuthorityDeletionEvent.builder().userId(userId).build();

        listener.onVerifierUserDeletedEvent(event);

        verify(verifierRequestTaskAssignmentService, times(1)).assignTasksOfDeletedVerifierToVbSiteContactOrRelease(userId);
        verify(systemMessageNotificationRequestService, times(1))
            .completeOpenSystemMessageNotificationRequests(userId);
        verifyNoMoreInteractions(systemMessageNotificationRequestService,  verifierRequestTaskAssignmentService);
    }
}
