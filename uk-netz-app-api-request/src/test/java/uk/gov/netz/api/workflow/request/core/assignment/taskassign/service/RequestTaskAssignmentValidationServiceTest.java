package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.OPERATOR;

@ExtendWith(MockitoExtension.class)
class RequestTaskAssignmentValidationServiceTest {

    @InjectMocks
    private RequestTaskAssignmentValidationService requestTaskAssignmentValidationService;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Mock
    private RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;

    @Test
    void validateTaskReleaseCapability() {
        RequestTask requestTask = buildMockRequestTask(new Request(), "DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", true);

        // Invoke
        requestTaskAssignmentValidationService.validateTaskReleaseCapability(requestTask, RoleTypeConstants.REGULATOR);
    }

    @Test
    void validateTaskReleaseCapability_task_operator() {
        RequestTask requestTask = buildMockRequestTask(new Request(), "DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", true);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
            requestTaskAssignmentValidationService.validateTaskReleaseCapability(requestTask, OPERATOR));

        // Assert
        assertEquals(ErrorCode.ASSIGNMENT_NOT_ALLOWED, businessException.getErrorCode());
    }

    @Test
    void validateTaskAssignmentCapability() {
        RequestTask requestTask = buildMockRequestTask(new Request(), "RequestTaskType.DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", true);

        // Invoke
        requestTaskAssignmentValidationService.validateTaskAssignmentCapability(requestTask);
    }

    @Test
    void hasUserPermissionsToBeAssignedToTask() {
        final String userId = "userId";
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        RequestTask requestTask = buildMockRequestTask(request, "RequestTaskType.DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", true);
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().roleType(OPERATOR).build();
        List<String> candidateAssignees = List.of("userId", "userId2");
        
        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, requestTask.getRequest().getAccountId().toString(),
                		ResourceType.CA, requestTask.getRequest().getCompetentAuthority().name()
                		))
                .build();

        // Mock
        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(userRoleTypeDTO);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                    requestTask.getType().getCode(), resourceCriteria, OPERATOR))
                .thenReturn(candidateAssignees);

        // Invoke
        boolean result = requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTask, userId);

        // Assert
        assertTrue(result);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                        requestTask.getType().getCode(), resourceCriteria, OPERATOR);
    }

    @Test
    void hasUserPermissionsToBeAssignedToTask_user_not_in_candidates() {
        final String userId = "userId";
        Request request = Request.builder().build();
        addResourcesToRequest(1L, CompetentAuthorityEnum.ENGLAND, request);
        RequestTask requestTask = buildMockRequestTask(request, "RequestTaskType.DUMMY_REQUEST_TYPE_APPLICATION_REVIEW", true);
        UserRoleTypeDTO userRoleTypeDTO = UserRoleTypeDTO.builder().roleType(OPERATOR).build();
        List<String> candidateAssignees = List.of("userId1", "userId2");
        
        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, requestTask.getRequest().getAccountId().toString(),
                		ResourceType.CA, requestTask.getRequest().getCompetentAuthority().name()
                		))
                .build();

        // Mock
        when(userRoleTypeService.getUserRoleTypeByUserId(userId)).thenReturn(userRoleTypeDTO);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                requestTask.getType().getCode(), resourceCriteria, OPERATOR))
                .thenReturn(candidateAssignees);

        // Invoke
        boolean result = requestTaskAssignmentValidationService.hasUserPermissionsToBeAssignedToTask(requestTask, userId);

        // Assert
        assertFalse(result);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                        requestTask.getType().getCode(), resourceCriteria, OPERATOR);
    }

    private RequestTask buildMockRequestTask(Request request, String type, boolean assignable) {
        return RequestTask.builder()
                .id(1L)
                .request(request)
                .type(RequestTaskType.builder().code(type).assignable(assignable).build())
                .build();
    }
    
    private void addResourcesToRequest(Long accountId, CompetentAuthorityEnum competentAuthority, Request request) {
		RequestResource caResource = RequestResource.builder()
				.resourceType(ResourceType.CA)
				.resourceId(competentAuthority.name())
				.request(request)
				.build();
		
		RequestResource accountResource = RequestResource.builder()
				.resourceType(ResourceType.ACCOUNT)
				.resourceId(accountId.toString())
				.request(request)
				.build();
		
        request.getRequestResources().addAll(List.of(caResource, accountResource));
	}
}
