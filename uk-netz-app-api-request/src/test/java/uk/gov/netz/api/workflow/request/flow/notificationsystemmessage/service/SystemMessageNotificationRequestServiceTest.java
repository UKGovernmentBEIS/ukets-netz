package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;

@ExtendWith(MockitoExtension.class)
class SystemMessageNotificationRequestServiceTest {

    @InjectMocks
    private SystemMessageNotificationRequestService cut;

    @Mock
    private RequestTaskRepository requestTaskRepository;

    @Mock
    private RequestTypeRepository requestTypeRepository;

    @Mock
    private WorkflowService workflowService;

    @Test
    void completeOpenSystemMessageNotificationRequests_by_assignee() {
        RequestType requestType = mock(RequestType.class);
        String assignee = "assignee";
        List<RequestTask> notificationRequestTasks = List.of(RequestTask.builder().processTaskId("pt1").build());

        when(requestTaskRepository
            .findByRequestTypeAndAssignee(requestType, assignee))
            .thenReturn(notificationRequestTasks);

        when(requestTypeRepository.findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION))
            .thenReturn(Optional.of(requestType));

        //invoke
        cut.completeOpenSystemMessageNotificationRequests(assignee);

        //verify
        verify(workflowService, times(1)).completeTask("pt1");
    }

    @Test
    void completeOpenSystemMessageNotificationRequests_by_assignee_and_account() {
        RequestType requestType = mock(RequestType.class);
        String assignee = "assignee";
        Long accountId = 1L;
        List<RequestTask> notificationRequestTasks = List.of(RequestTask.builder().processTaskId("pt1").build());

        when(requestTaskRepository
            .findByRequestTypeAndAssigneeAndRequestAccountId(requestType, assignee, accountId))
            .thenReturn(notificationRequestTasks);
        when(requestTypeRepository.findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION))
            .thenReturn(Optional.of(requestType));

        //invoke
        cut.completeOpenSystemMessageNotificationRequests(assignee, accountId);

        //verify
        verify(workflowService, times(1)).completeTask("pt1");
    }
}
