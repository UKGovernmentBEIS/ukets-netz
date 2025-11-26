package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.regulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountCaSiteContactService;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.exception.BusinessCheckedException;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.assignment.requestassign.release.RequestReleaseService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.RequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.REGULATOR;

@ExtendWith(MockitoExtension.class)
class RegulatorRequestTaskDefaultAssignmentServiceTest {

    @InjectMocks
    private RegulatorRequestTaskDefaultAssignmentService regulatorRequestTaskDefaultAssignmentService;

    @Mock
    private RequestTaskAssignmentService requestTaskAssignmentService;

    @Mock
    private AccountCaSiteContactService accountCaSiteContactService;

    @Mock
    private RequestReleaseService requestReleaseService;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Test
    void assignDefaultAssigneeToTask_non_peer_review_task() throws BusinessCheckedException {
        String requestRegulatorAssignee = "requestRegulatorAssignee";
        String requestRegulatorReviewer = "requestRegulatorReviewer";
        String regulatorPeerReviewer = "regulatorPeerReviewer";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorAssignee(requestRegulatorAssignee)
                .regulatorReviewer(requestRegulatorReviewer)
                .regulatorPeerReviewer(regulatorPeerReviewer)
                .build())
            .build();
        RequestTask requestTask = RequestTask.builder().request(request).type(RequestTaskType.builder().code("requestTaskType").build()).build();

        when(userRoleTypeService.isUserRegulator(requestRegulatorAssignee)).thenReturn(true);

        regulatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserRegulator(requestRegulatorAssignee);
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, requestRegulatorAssignee);
        verifyNoInteractions(accountCaSiteContactService, requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_non_peer_review_task_assignment_throws_exception() throws BusinessCheckedException {
        String requestRegulatorAssignee = "requestRegulatorAssignee";
        String requestRegulatorReviewer = "requestRegulatorReviewer";
        String regulatorPeerReviewer = "regulatorPeerReviewer";
        String caSiteContact = "caSiteContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorAssignee(requestRegulatorAssignee)
                .regulatorReviewer(requestRegulatorReviewer)
                .regulatorPeerReviewer(regulatorPeerReviewer)
                .build())
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder().request(request).type(RequestTaskType.builder().code("requestTaskType").build()).build();

        when(userRoleTypeService.isUserRegulator(requestRegulatorAssignee)).thenReturn(true);
        doThrow(BusinessCheckedException.class).when(requestTaskAssignmentService).assignToUser(requestTask, requestRegulatorAssignee);
        when(accountCaSiteContactService.findCASiteContactByAccount(request.getAccountId())).thenReturn(Optional.of(caSiteContact));

        regulatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserRegulator(requestRegulatorAssignee);
        verify(accountCaSiteContactService, times(1)).findCASiteContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, requestRegulatorAssignee);
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, caSiteContact);
        verifyNoInteractions(requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_non_peer_review_task_regulator_assignee_not_exists() throws BusinessCheckedException {
        String requestRegulatorReviewer = "requestRegulatorReviewer";
        String regulatorPeerReviewer = "regulatorPeerReviewer";
        String caSiteContact = "caSiteContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorReviewer(requestRegulatorReviewer)
                .regulatorPeerReviewer(regulatorPeerReviewer)
                .build())
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder().request(request).type(RequestTaskType.builder().code("requestTaskType").build()).build();

        when(accountCaSiteContactService.findCASiteContactByAccount(request.getAccountId())).thenReturn(Optional.of(caSiteContact));

        regulatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(accountCaSiteContactService, times(1)).findCASiteContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, caSiteContact);
        verifyNoMoreInteractions(requestTaskAssignmentService);
        verifyNoInteractions(userRoleTypeService, requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_non_peer_review_task_regulator_assignee_not_reviewer() throws BusinessCheckedException {
        String requestRegulatorAssignee = "requestRegulatorAssignee";
        String requestRegulatorReviewer = "requestRegulatorReviewer";
        String regulatorPeerReviewer = "regulatorPeerReviewer";
        String caSiteContact = "caSiteContact";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorAssignee(requestRegulatorAssignee)
                .regulatorReviewer(requestRegulatorReviewer)
                .regulatorPeerReviewer(regulatorPeerReviewer)
                .build())
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder().request(request).type(RequestTaskType.builder().code("requestTaskType").build()).build();

        when(userRoleTypeService.isUserRegulator(requestRegulatorAssignee)).thenReturn(false);
        when(accountCaSiteContactService.findCASiteContactByAccount(request.getAccountId())).thenReturn(Optional.of(caSiteContact));

        regulatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(userRoleTypeService, times(1)).isUserRegulator(requestRegulatorAssignee);
        verify(accountCaSiteContactService, times(1)).findCASiteContactByAccount(request.getAccountId());
        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, caSiteContact);
        verifyNoMoreInteractions(requestTaskAssignmentService);
        verifyNoInteractions(requestReleaseService);
    }

    @Test
    void assignDefaultAssigneeToTask_non_peer_review_task_nor_regulator_assignee_neither_site_contact_exist() {
        String requestSupportingRegulator = "requestSupportingRegulator";
        String regulatorPeerReviewer = "regulatorPeerReviewer";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorPeerReviewer(requestSupportingRegulator)
                .regulatorReviewer(regulatorPeerReviewer)
                .build())
            .build();
        addAccountResourceToRequest(1L, request);
        RequestTask requestTask = RequestTask.builder().request(request).type(RequestTaskType.builder().code("requestTaskType").build()).build();

        when(accountCaSiteContactService.findCASiteContactByAccount(request.getAccountId())).thenReturn(Optional.empty());

        regulatorRequestTaskDefaultAssignmentService.assignDefaultAssigneeToTask(requestTask);

        verify(accountCaSiteContactService, times(1)).findCASiteContactByAccount(request.getAccountId());
        verify(requestReleaseService, times(1)).releaseRequest(requestTask);
        verifyNoInteractions(userRoleTypeService, requestTaskAssignmentService);
    }

    @Test
    void getRoleType() {
        assertEquals(REGULATOR, regulatorRequestTaskDefaultAssignmentService.getRoleType());
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