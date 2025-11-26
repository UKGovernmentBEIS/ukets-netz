package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessCheckedException;
import uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign.RequestAssignmentService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.common.EmailNotificationAssignedTaskService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.TestRequestTaskPayload;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskAssignmentServiceTest {

    @InjectMocks
    private RequestTaskAssignmentService requestTaskAssignmentService;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private RequestTaskAssignmentValidationService requestTaskAssignmentValidationService;

    @Mock
    private RequestAssignmentService requestAssignmentService;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Mock
    private EmailNotificationAssignedTaskService emailNotificationAssignedTaskService;

    @Test
    void assignToUser() throws BusinessCheckedException {
        String userId = "userId";
        String anotherUserId = "anotherUserId";
        Request request = Request.builder().id("1").build();
        RequestTask requestTaskToBeAssigned = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .assignee(null)
            .build();
        RequestTask requestTask1 = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .assignee(anotherUserId)
            .build();
        RequestTask requestTask2 = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .assignee(userId)
            .build();

        List<RequestTask> requestTasks = new ArrayList<>(Arrays.asList(requestTaskToBeAssigned, requestTask1, requestTask2));
        UserRoleTypeDTO userRoleType = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();

        when(requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTaskToBeAssigned, userId))
            .thenReturn(true);
        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(userRoleType);
        when(requestTaskService.findTasksByRequestIdAndRoleType(request.getId(), userRoleType.getRoleType()))
            .thenReturn(requestTasks);
        when(requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTask1, userId)).thenReturn(true);

        requestTaskAssignmentService.assignToUser(requestTaskToBeAssigned, userId);

        verify(requestTaskAssignmentValidationService, times(2))
            .hasUserPermissionsToBeAssignedToTask(any(), anyString());
        verify(userRoleTypeService, times(1)).getUserRoleTypeByUserId(userId);
        verify(requestTaskService, times(1))
            .findTasksByRequestIdAndRoleType(request.getId(), userRoleType.getRoleType());
        verify(requestAssignmentService, times(1))
            .assignRequestToUser(requestTaskToBeAssigned.getRequest(), userId);
        verify(emailNotificationAssignedTaskService, times(1)).sendEmailToRecipient(userId);
        verifyNoMoreInteractions(requestTaskAssignmentValidationService, emailNotificationAssignedTaskService);
    }

    @Test
    void assignToUser_do_not_send_email() throws BusinessCheckedException {
        String userId = "userId";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
                .type(RequestTaskType.builder().code("requestTaskType").build())
                .request(request)
                .payload(TestRequestTaskPayload.builder().sendEmailNotification(false).build())
                .build();
        UserRoleTypeDTO userRoleType = UserRoleTypeDTO.builder().userId(userId).roleType(RoleTypeConstants.REGULATOR).build();
        List<RequestTask> requestTasks = new ArrayList<>(Collections.singletonList(requestTask));

        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(userRoleType);
        when(requestTaskService.findTasksByRequestIdAndRoleType(request.getId(), userRoleType.getRoleType())).thenReturn(requestTasks);
        when(requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTask, userId)).thenReturn(true);

        requestTaskAssignmentService.assignToUser(requestTask, userId);
        verify(emailNotificationAssignedTaskService, never()).sendEmailToRecipient(userId);

    }

    @Test
    void assignToUser_user_has_no_permission() {
        String userId = "userId";
        RequestTask requestTask = RequestTask.builder().build();

        when(requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTask, userId))
            .thenReturn(false);

        assertThrows(BusinessCheckedException.class,
            () -> requestTaskAssignmentService.assignToUser(requestTask, userId));

        verify(requestTaskAssignmentValidationService, times(1))
            .hasUserPermissionsToBeAssignedToTask(requestTask, userId);
        verifyNoMoreInteractions(requestTaskAssignmentValidationService);
        verifyNoInteractions(requestTaskService, requestAssignmentService, userRoleTypeService,
            emailNotificationAssignedTaskService);
    }
}
