package uk.gov.netz.api.workflow.request.application.userdeleted;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.regulator.event.RegulatorAuthorityDeletionEvent;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.regulator.RegulatorRequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service.SystemMessageNotificationRequestService;

@ExtendWith(MockitoExtension.class)
class RegulatorAuthorityDeletionEventListenerTest {

    @InjectMocks
    private RegulatorAuthorityDeletionEventListener listener;

    @Mock
    private RegulatorRequestTaskAssignmentService regulatorRequestTaskAssignmentService;

    @Mock
    private SystemMessageNotificationRequestService systemMessageNotificationRequestService;

    @Test
    void onRegulatorUserDeletedEvent() {
        final String userId = "user";
        RegulatorAuthorityDeletionEvent event = RegulatorAuthorityDeletionEvent.builder().userId(userId).build();

        listener.onRegulatorUserDeletedEvent(event);

        verify(regulatorRequestTaskAssignmentService, times(1)).assignTasksOfDeletedRegulatorToCaSiteContactOrRelease(userId);
        verify(systemMessageNotificationRequestService, times(1))
            .completeOpenSystemMessageNotificationRequests(userId);
        verifyNoMoreInteractions(systemMessageNotificationRequestService,  regulatorRequestTaskAssignmentService);
    }
}
