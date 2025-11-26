package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.operator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountContactQueryService;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessCheckedException;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.assignment.requestassign.release.RequestReleaseService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.RequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestStatuses;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorRequestTaskDefaultAssignmentServiceTest {

    @InjectMocks
    private OperatorRequestTaskDefaultAssignmentService operatorRequestTaskDefaultAssignmentService;

    @Mock
    private RequestTaskAssignmentService requestTaskAssignmentService;

    @Mock
    private AccountContactQueryService accountContactQueryService;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Mock
    private RequestReleaseService requestReleaseService;

    @Test
    void assignDefaultAssigneeToTask() throws BusinessCheckedException {
        String operatorAssignee = "operatorAssignee";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().operatorAssignee(operatorAssignee).build())
            .status(RequestStatuses.IN_PROGRESS)
            .build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(userRoleTypeService.isUserOperator(operatorAssignee)).thenReturn(true);
        doNothing().when(requestTaskAssignmentService).assignToUser(requestTask, operatorAssignee);

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserOperator(operatorAssignee);
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, operatorAssignee);
        verifyNoMoreInteractions(requestTaskAssignmentService);
        verifyNoInteractions(accountContactQueryService, requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_null_request_assignee() throws BusinessCheckedException {
        String primaryContact = "primaryContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().build())
            .status(RequestStatuses.IN_PROGRESS).build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(accountContactQueryService.findPrimaryContactByAccount(request.getAccountId()))
            .thenReturn(Optional.of(primaryContact));

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(accountContactQueryService, times(1)).findPrimaryContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, primaryContact);
        verifyNoMoreInteractions(requestTaskAssignmentService);
        verifyNoInteractions(userRoleTypeService, requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_request_assignee_is_not_operator() throws BusinessCheckedException {
        String operatorAssignee = "operatorAssignee";
        String primaryContact = "primaryContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().operatorAssignee(operatorAssignee).build())
            .status(RequestStatuses.IN_PROGRESS)
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(userRoleTypeService.isUserOperator(operatorAssignee)).thenReturn(false);
        when(accountContactQueryService.findPrimaryContactByAccount(request.getAccountId())).
            thenReturn(Optional.of(primaryContact));

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserOperator(operatorAssignee);
        verify(accountContactQueryService, times(1)).findPrimaryContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, primaryContact);
        verifyNoMoreInteractions(requestTaskAssignmentService);
        verifyNoInteractions(requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_assign_to_request_assignee_throws_business_exception() throws BusinessCheckedException {
        String operatorAssignee = "operatorAssignee";
        String primaryContact = "primaryContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().operatorAssignee(operatorAssignee).build())
            .status(RequestStatuses.IN_PROGRESS)
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(userRoleTypeService.isUserOperator(operatorAssignee)).thenReturn(true);
        doThrow(BusinessCheckedException.class).when(requestTaskAssignmentService).assignToUser(requestTask, operatorAssignee);
        when(accountContactQueryService.findPrimaryContactByAccount(request.getAccountId()))
            .thenReturn(Optional.of(primaryContact));

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserOperator(operatorAssignee);
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, operatorAssignee);
        verify(accountContactQueryService, times(1)).findPrimaryContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, primaryContact);
        verifyNoInteractions(requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_assign_to_primary_account_throws_business_exception() throws BusinessCheckedException {
        String operatorAssignee = "operatorAssignee";
        String primaryContact = "primaryContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().operatorAssignee(operatorAssignee).build())
            .status(RequestStatuses.IN_PROGRESS)
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(userRoleTypeService.isUserOperator(operatorAssignee)).thenReturn(true);
        doThrow(BusinessCheckedException.class).when(requestTaskAssignmentService).assignToUser(requestTask, operatorAssignee);
        when(accountContactQueryService.findPrimaryContactByAccount(request.getAccountId()))
            .thenReturn(Optional.of(primaryContact));
        doThrow(BusinessCheckedException.class).when(requestTaskAssignmentService).assignToUser(requestTask, primaryContact);

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserOperator(operatorAssignee);
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, operatorAssignee);
        verify(accountContactQueryService, times(1)).findPrimaryContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, primaryContact);
        verify(requestReleaseService, times(1)).releaseRequest(requestTask);
    }

    @Test
    void assignDefaultAssigneeToTask_null_request_assignee_and_no_primary_contact() {
        Request request = Request.builder()
            .payload(TestRequestPayload.builder().build())
            .status(RequestStatuses.IN_PROGRESS).build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").build())
            .build();

        when(accountContactQueryService.findPrimaryContactByAccount(request.getAccountId())).thenReturn(Optional.empty());

        operatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(accountContactQueryService, times(1)).findPrimaryContactByAccount(request.getAccountId());
        verify(requestReleaseService, times(1)).releaseRequest(requestTask);
        verifyNoInteractions(userRoleTypeService, requestTaskAssignmentService);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.OPERATOR, operatorRequestTaskDefaultAssignmentService.getRoleType());
    }
    
    private void addAccountResourceToRequest(Long accountId, Request request) {	
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();
        request.getRequestResources().add(accountResource);
	}
}