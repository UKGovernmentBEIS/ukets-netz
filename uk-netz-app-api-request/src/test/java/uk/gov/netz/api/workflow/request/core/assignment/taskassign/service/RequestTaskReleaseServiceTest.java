package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.assignment.requestassign.release.RequestReleaseService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskReleaseServiceTest {

    @InjectMocks
    private RequestTaskReleaseService requestTaskReleaseService;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private RequestTaskAssignmentValidationService requestTaskAssignmentValidationService;

    @Mock
    private RequestReleaseService requestReleaseService;

    @Mock
    private AuthorizationRulesQueryService authorizationRulesQueryService;
    
    @Test
    void releaseTaskById_not_task_release_capability() {
        Long requestTaskId = 1L;
        Request request = Request.builder().id("1").build();
        RequestTask requestTask = RequestTask.builder().id(requestTaskId).request(request)
                .type(RequestTaskType.builder().code("type").build()).assignee("assignee").build();
        String roleType = RoleTypeConstants.OPERATOR;

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(authorizationRulesQueryService.findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, requestTask.getType().getCode()))
                .thenReturn(Optional.of(RoleTypeConstants.OPERATOR));
        doThrow(new BusinessException(ErrorCode.ASSIGNMENT_NOT_ALLOWED))
                .when(requestTaskAssignmentValidationService).validateTaskReleaseCapability(requestTask, roleType);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                requestTaskReleaseService.releaseTaskById(requestTaskId));

        // Verify
        assertEquals(ErrorCode.ASSIGNMENT_NOT_ALLOWED, businessException.getErrorCode());

        verify(requestTaskService, times(1))
                .findTaskById(requestTaskId);
        verify(authorizationRulesQueryService, times(1))
                .findRoleTypeByResourceTypeAndSubType(ResourceType.REQUEST_TASK, requestTask.getType().getCode());
        verify(requestTaskAssignmentValidationService, times(1))
                .validateTaskReleaseCapability(requestTask, roleType);
        verify(requestTaskService, never())
                .findTasksByRequestIdAndRoleType(anyString(), any());
        verify(requestReleaseService, never()).releaseRequest(any());

        assertNotNull(requestTask.getAssignee());
    }
    
    @Test
    void releaseTaskForced() {
        Long requestTaskId = 1L;
        Request request = Request.builder().id("1").build();
        RequestTask requestTask = RequestTask.builder().id(requestTaskId).request(request).assignee("assignee").build();

        // Invoke
        requestTaskReleaseService.releaseTaskForced(requestTask);

        // Verify
        ArgumentCaptor<RequestTask> requestTaskCaptor = ArgumentCaptor.forClass(RequestTask.class);
        verify(requestReleaseService, times(1)).releaseRequest(requestTaskCaptor.capture());
        assertNull(requestTaskCaptor.getValue().getAssignee());
        verifyNoInteractions(requestTaskAssignmentValidationService);
    }

}
