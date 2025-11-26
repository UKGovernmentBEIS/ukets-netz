package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.dto.AssigneeUserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskTypeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.OPERATOR;
import static uk.gov.netz.api.common.constants.RoleTypeConstants.REGULATOR;
import static uk.gov.netz.api.competentauthority.CompetentAuthorityEnum.ENGLAND;

@ExtendWith(MockitoExtension.class)
class RequestTaskAssignmentQueryServiceTest {

    @InjectMocks
    private RequestTaskAssignmentQueryService requestTaskAssignmentQueryService;

    @Mock
    private RequestTaskService requestTaskService;
    
    @Mock
    private RequestTaskTypeService requestTaskTypeService;

    @Mock
    private UserInfoApi userInfoApi;

    @Mock
    private RequestTaskAssignmentValidationService requestTaskAssignmentValidationService;

    @Mock
    private RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;

    @Test
    void getCandidateAssigneesByTaskId_non_peer_review_task() {
        final Long requestTaskId = 1L;
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = ENGLAND;
        final String userRole = OPERATOR;
        final String requestRegulatorReviewer = "requestRegulatorReviewer";
        Request request = Request.builder()
                .payload(TestRequestPayload.builder().regulatorReviewer(requestRegulatorReviewer).build())
                .build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .type(RequestTaskType.builder().code("requestTaskType").build())
                .request(request).build();
        AppUser user = AppUser.builder().roleType(userRole).build();
        List<String> candidateAssignees = List.of("userId1", "userId2");
        List<UserInfo> users = buildMockUserInfoList(candidateAssignees);
        List<AssigneeUserInfoDTO> candidateAssigneesInfo = buildMockAssigneeUserInfoList(candidateAssignees);

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        // Mock
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                requestTask.getType().getCode(), resourceCriteria, userRole)).thenReturn(candidateAssignees);
        when(userInfoApi.getUsers(candidateAssignees)).thenReturn(users);

        // Invoke
        List<AssigneeUserInfoDTO> actualUsersInfo = requestTaskAssignmentQueryService.getCandidateAssigneesByTaskId(requestTaskId, user);

        // Assert
        assertEquals(candidateAssigneesInfo, actualUsersInfo);
        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTask);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTask.getType().getCode(), resourceCriteria, userRole);
        verify(userInfoApi, times(1)).getUsers(candidateAssignees);
    }

    @Test
    void getCandidateAssigneesByTaskId_peer_review_task() {
        final Long requestTaskId = 1L;
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = ENGLAND;
        final String userRole = REGULATOR;
        final String requestRegulatorReviewer = "requestRegulatorReviewer";
        final String userId1 = "userId1";
        final String userId2 = "userId2";
        Request request = Request.builder()
                .payload(TestRequestPayload.builder().regulatorReviewer(requestRegulatorReviewer).build())
                .build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .type(RequestTaskType.builder().code("requestTaskType").build())
                .request(request).build();
        AppUser user = AppUser.builder().roleType(userRole).build();
        List<String> candidateAssignees = new ArrayList<>(Arrays.asList(requestRegulatorReviewer, userId1, userId2));
        List<String> peerReviewTaskCandidateAssignees = new ArrayList<>(Arrays.asList(userId1, userId2));
        List<UserInfo> users = buildMockUserInfoList(peerReviewTaskCandidateAssignees);
        List<AssigneeUserInfoDTO> candidateAssigneesInfo = buildMockAssigneeUserInfoList(peerReviewTaskCandidateAssignees);

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        // Mock
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                requestTask.getType().getCode(), resourceCriteria, userRole)).thenReturn(candidateAssignees);
        when(userInfoApi.getUsers(candidateAssignees)).thenReturn(users);

        // Invoke
        List<AssigneeUserInfoDTO> actualUsersInfo = requestTaskAssignmentQueryService.getCandidateAssigneesByTaskId(requestTaskId, user);

        // Assert
        assertEquals(candidateAssigneesInfo, actualUsersInfo);
        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTask);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTask.getType().getCode(), resourceCriteria, userRole);
        verify(userInfoApi, times(1)).getUsers(candidateAssignees);
    }

    @Test
    void getCandidateAssigneesByTaskId_not_valid_task_capability() {
        final Long requestTaskId = 1L;
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = ENGLAND;
        Request request = Request.builder().build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .request(request).build();
        AppUser user = AppUser.builder().roleType(OPERATOR).build();

        // Mock
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        doThrow(new BusinessException(ErrorCode.ASSIGNMENT_NOT_ALLOWED))
                .when(requestTaskAssignmentValidationService).validateTaskAssignmentCapability(requestTask);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                requestTaskAssignmentQueryService.getCandidateAssigneesByTaskId(requestTaskId, user));

        // Assert
        assertEquals(ErrorCode.ASSIGNMENT_NOT_ALLOWED, businessException.getErrorCode());
        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTask);
        verify(requestTaskAuthorizationResourceService, never())
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(anyString(), any(), any());
        verify(userInfoApi, never()).getUsers(anyList());
    }

    @Test
    void getCandidateAssigneesByTaskId_empty_users() {
        final Long requestTaskId = 1L;
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = ENGLAND;
        Request request = Request.builder().build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .type(RequestTaskType.builder().code("requestTaskType").build())
                .request(request).build();
        AppUser user = AppUser.builder().roleType(OPERATOR).build();
        List<String> candidateAssignees = List.of();
        List<UserInfo> users = List.of();
        List<AssigneeUserInfoDTO> candidateAssigneesInfo = List.of();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();
        
        // Mock
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                requestTask.getType().getCode(), resourceCriteria, OPERATOR)).thenReturn(candidateAssignees);
        when(userInfoApi.getUsers(candidateAssignees)).thenReturn(users);

        // Invoke
        List<AssigneeUserInfoDTO> actualUsersInfo = requestTaskAssignmentQueryService.getCandidateAssigneesByTaskId(requestTaskId, user);

        // Assert
        assertEquals(candidateAssigneesInfo, actualUsersInfo);
        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTask);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(requestTask.getType().getCode(), resourceCriteria, OPERATOR);
        verify(userInfoApi, times(1)).getUsers(candidateAssignees);
    }

    @Test
    void getCandidateAssigneesByTaskType_supporting_task() {
        Long requestTaskId = 1L;
        Long accountId = 101L;
        final RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskTypeCode").build();
        final CompetentAuthorityEnum ca = ENGLAND;
        final String appUserRole = REGULATOR;
        final String appUserId = "userId";
        final String userId1 = "userId1";
        final String userId2 = "userId2";
        Request request = Request.builder().build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .request(request).type(requestTaskType).build();
        AppAuthority appAuthority = AppAuthority.builder()
                .competentAuthority(ca)
                .build();
        AppUser appUser = AppUser.builder().userId(appUserId).roleType(appUserRole).authorities(List.of(appAuthority)).build();
        List<String> candidateAssignees = new ArrayList<>(Arrays.asList(appUserId, userId1, userId2));
        List<String> peerReviewTaskCandidateAssignees = new ArrayList<>(Arrays.asList(userId1, userId2));
        List<UserInfo> users = buildMockUserInfoList(peerReviewTaskCandidateAssignees);
        List<AssigneeUserInfoDTO> candidateAssigneesInfo = buildMockAssigneeUserInfoList(peerReviewTaskCandidateAssignees);

        String taskTypeToFindCandidates = "taskTypeToFindCandidates";
        RequestTaskType requestTaskTypeToFindCandidates = RequestTaskType.builder().code(taskTypeToFindCandidates).build();
        
        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        // Mock
        when(requestTaskTypeService.findByCode(taskTypeToFindCandidates)).thenReturn(requestTaskTypeToFindCandidates);
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
        		taskTypeToFindCandidates, resourceCriteria, appUserRole)).thenReturn(candidateAssignees);
        when(userInfoApi.getUsers(candidateAssignees)).thenReturn(users);

        // Invoke
        List<AssigneeUserInfoDTO> actualUsersInfo = requestTaskAssignmentQueryService
                .getCandidateAssigneesByTaskType(requestTaskId, taskTypeToFindCandidates, appUser);

        // Assert
        assertEquals(candidateAssigneesInfo, actualUsersInfo);
        verify(requestTaskTypeService,  times(1)).findByCode(taskTypeToFindCandidates);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTaskTypeToFindCandidates);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(taskTypeToFindCandidates, resourceCriteria, appUserRole);
        verify(userInfoApi, times(1)).getUsers(candidateAssignees);
    }

    @Test
    void getCandidateAssigneesByTaskType_non_supporting_task() {
        Long requestTaskId = 1L;
        Long accountId = 101L;
        final RequestTaskType requestTaskType = RequestTaskType.builder().code("requestTaskTypeCode").build();
        final CompetentAuthorityEnum ca = ENGLAND;
        final String appUserRole = REGULATOR;
        final String appUserId = "userId";
        final String userId1 = "userId1";
        final String userId2 = "userId2";
        Request request = Request.builder().build();
        addResourcesToRequest(accountId, ca, request);
        RequestTask requestTask = RequestTask.builder().id(requestTaskId)
                .request(request).type(requestTaskType).build();
        AppAuthority appAuthority = AppAuthority.builder()
                .competentAuthority(ca)
                .build();
        AppUser appUser = AppUser.builder().userId(appUserId).roleType(appUserRole).authorities(List.of(appAuthority)).build();
        List<String> candidateAssignees = new ArrayList<>(Arrays.asList(appUserId, userId1, userId2));
        List<UserInfo> users = buildMockUserInfoList(candidateAssignees);
        List<AssigneeUserInfoDTO> candidateAssigneesInfo = buildMockAssigneeUserInfoList(candidateAssignees);
        
        String taskTypeToFindCandidates = "taskTypeToFindCandidates";
        RequestTaskType requestTaskTypeToFindCandidates = RequestTaskType.builder().code(taskTypeToFindCandidates).build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        // Mock
        when(requestTaskTypeService.findByCode(taskTypeToFindCandidates)).thenReturn(requestTaskTypeToFindCandidates);
        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
        		taskTypeToFindCandidates, resourceCriteria, appUserRole)).thenReturn(candidateAssignees);
        when(userInfoApi.getUsers(candidateAssignees)).thenReturn(users);

        // Invoke
        List<AssigneeUserInfoDTO> actualUsersInfo = requestTaskAssignmentQueryService
                .getCandidateAssigneesByTaskType(requestTaskId, taskTypeToFindCandidates, appUser);

        // Assert
        assertEquals(candidateAssigneesInfo, actualUsersInfo);
        verify(requestTaskTypeService,  times(1)).findByCode(taskTypeToFindCandidates);
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTaskTypeToFindCandidates);
        verify(requestTaskAuthorizationResourceService, times(1))
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(taskTypeToFindCandidates, resourceCriteria, appUserRole);
        verify(userInfoApi, times(1)).getUsers(candidateAssignees);
    }

    @Test
    void getCandidateAssigneesByTaskType_task_type_not_assignable() {
        Long requestTaskId = 1L;
        final String appUserId = "userId";
        AppUser appUser = AppUser.builder().userId(appUserId).build();

        String taskTypeToFindCandidates = "taskTypeToFindCandidates";
        RequestTaskType requestTaskTypeToFindCandidates = RequestTaskType.builder().code(taskTypeToFindCandidates).build();
        
        // Mock
        when(requestTaskTypeService.findByCode(taskTypeToFindCandidates)).thenReturn(requestTaskTypeToFindCandidates);
        doThrow(new BusinessException(ErrorCode.REQUEST_TASK_NOT_ASSIGNABLE))
                .when(requestTaskAssignmentValidationService).validateTaskAssignmentCapability(requestTaskTypeToFindCandidates);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                requestTaskAssignmentQueryService.getCandidateAssigneesByTaskType(requestTaskId, taskTypeToFindCandidates, appUser));

        // Assert
        assertEquals(ErrorCode.REQUEST_TASK_NOT_ASSIGNABLE, businessException.getErrorCode());
        verify(requestTaskTypeService,  times(1)).findByCode(taskTypeToFindCandidates);
        verify(requestTaskService, never()).findTaskById(anyLong());
        verify(requestTaskAssignmentValidationService, times(1)).validateTaskAssignmentCapability(requestTaskTypeToFindCandidates);
        verifyNoInteractions(requestTaskAuthorizationResourceService, userInfoApi);
    }

    private List<UserInfo> buildMockUserInfoList(List<String> userIds) {
        return userIds.stream()
                .map(userId -> UserInfo.builder().id(userId).firstName(userId).lastName(userId).build())
                .collect(Collectors.toList());
    }

    private List<AssigneeUserInfoDTO> buildMockAssigneeUserInfoList(List<String> userIds) {
        return userIds.stream()
                .map(userId -> AssigneeUserInfoDTO.builder().id(userId).firstName(userId).lastName(userId).build())
                .collect(Collectors.toList());
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
