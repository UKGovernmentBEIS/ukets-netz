package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.regulator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessCheckedException;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.RequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.SiteContactRequestTaskAssignmentService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.enumeration.SupportingTaskType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RegulatorRequestTaskAssignmentServiceTest {

    @InjectMocks
    private RegulatorRequestTaskAssignmentService regulatorRequestTaskAssignmentService;

    @Mock
    private SiteContactRequestTaskAssignmentService siteContactRequestTaskAssignmentService;

    @Mock
    private RequestTaskAssignmentService requestTaskAssignmentService;

    @Test
    void assignTasksOfDeletedRegulatorToCaSiteContactOrRelease() {
        String userId = "userId";
        regulatorRequestTaskAssignmentService.assignTasksOfDeletedRegulatorToCaSiteContactOrRelease(userId);
        verify(siteContactRequestTaskAssignmentService, times(1))
            .assignTasksOfDeletedUserToSiteContactOrRelease(userId, AccountContactType.CA_SITE);
    }

    @Test
    void assignTask_peer_review_task() throws BusinessCheckedException {
        String userId = "userId";
        String requestRegulatorReviewer = "requestRegulatorReviewer";
        Request request = Request.builder()
            .payload(TestRequestPayload.builder()
                .regulatorReviewer(requestRegulatorReviewer)
                .build())
            .build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskType").supporting(SupportingTaskType.PEER_REVIEW).build())
            .build();

        regulatorRequestTaskAssignmentService.assignTask(requestTask, userId);

        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, userId);
        verifyNoMoreInteractions(requestTaskAssignmentService);
    }

    @Test
    void assignTask_non_supporting_task() throws BusinessCheckedException {
        String userId = "userId";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskTypeCode").build())
            .build();

        regulatorRequestTaskAssignmentService.assignTask(requestTask, userId);

        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, userId);
    }

    @Test
    void assignTask_non_supporting_task_exception_on_assignment() throws BusinessCheckedException {
        String userId = "userId";
        Request request = Request.builder().build();
        RequestTask requestTask = RequestTask.builder()
            .request(request)
            .type(RequestTaskType.builder().code("requestTaskTypeCode").build())
            .build();

        doThrow(BusinessCheckedException.class).when(requestTaskAssignmentService).assignToUser(requestTask, userId);

        BusinessException businessException = assertThrows(BusinessException.class,
            () -> regulatorRequestTaskAssignmentService.assignTask(requestTask, userId));

        assertEquals(ErrorCode.ASSIGNMENT_NOT_ALLOWED, businessException.getErrorCode());

        verify(requestTaskAssignmentService, times(1)).assignToUser(requestTask, userId);
        verifyNoMoreInteractions(requestTaskAssignmentService);
    }

    @Test
    void getRoleType() {
        assertEquals(RoleTypeConstants.REGULATOR, regulatorRequestTaskAssignmentService.getRoleType());
    }
}