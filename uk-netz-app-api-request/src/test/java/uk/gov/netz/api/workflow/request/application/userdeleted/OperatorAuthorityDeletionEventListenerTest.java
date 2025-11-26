package uk.gov.netz.api.workflow.request.application.userdeleted;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.operator.OperatorRequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service.SystemMessageNotificationRequestService;

@ExtendWith(MockitoExtension.class)
class OperatorAuthorityDeletionEventListenerTest {

    @InjectMocks
    private OperatorAuthorityDeletionEventListener operatorAuthorityDeletionEventListener;

    @Mock
    private OperatorRequestTaskAssignmentService operatorRequestTaskAssignmentService;

    @Mock
    private SystemMessageNotificationRequestService systemMessageNotificationRequestService;

    @Test
    void onOperatorUserDeletionEvent() {
        String userId = "userId";
        Long accountId = 1L;
        uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent deletionEvent = uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent.builder()
            .accountId(accountId)
            .userId(userId)
            .build();

        operatorAuthorityDeletionEventListener.onOperatorUserDeletionEvent(deletionEvent);

        verify(systemMessageNotificationRequestService, times(1))
            .completeOpenSystemMessageNotificationRequests(userId, accountId);
        verify(operatorRequestTaskAssignmentService, times(1))
            .assignUserTasksToAccountPrimaryContactOrRelease(userId, accountId);
        verifyNoMoreInteractions(systemMessageNotificationRequestService,  operatorRequestTaskAssignmentService);
    }
}