package uk.gov.netz.api.workflow.request.application.userdeleted;

import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.operator.event.OperatorAuthorityDeletionEvent;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.operator.OperatorRequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service.SystemMessageNotificationRequestService;

@RequiredArgsConstructor
@Component(value =  "workflowOperatorAuthorityDeletionEventListener")
public class OperatorAuthorityDeletionEventListener {

    private final OperatorRequestTaskAssignmentService operatorRequestTaskAssignmentService;
    private final SystemMessageNotificationRequestService systemMessageNotificationRequestService;

    @Order(2)
    @EventListener(OperatorAuthorityDeletionEvent.class)
    public void onOperatorUserDeletionEvent(OperatorAuthorityDeletionEvent deletionEvent) {
        String deletedUserId = deletionEvent.getUserId();
        Long accountId = deletionEvent.getAccountId();
        systemMessageNotificationRequestService.completeOpenSystemMessageNotificationRequests(deletedUserId, accountId);
        operatorRequestTaskAssignmentService.assignUserTasksToAccountPrimaryContactOrRelease(deletedUserId, accountId);
    }
}
