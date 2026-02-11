package uk.gov.netz.api.workflow.request.application.taskview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.user.application.UserServiceDelegator;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.user.regulator.domain.RegulatorUserDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.TestRequestTaskPayload;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTaskViewServiceTest {

    @InjectMocks
    private RequestTaskViewService service;

    @Mock
    private RequestTaskService requestTaskService;

    @Mock
    private UserServiceDelegator userServiceDelegator;

    @Mock
    private RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;

    @Mock
    private RequestTaskEligibleActionsResolver resolver;


    @Test
    void getTaskItemInfo() {
        final String user = "user";
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final AppUser appUser = AppUser.builder().userId(user).firstName("fn").lastName("ln").roleType(RoleTypeConstants.REGULATOR).build();
        final Long requestTaskId = 1L;
        final String requestTypeCode = "requestTypeCode";
        final String requestTaskTypeCode = "requestTaskTypeCode";
        List<String> allowedRequestTaskActions = List.of("action1", "action2");

        final Request request = createRequest("1", ca, accountId, requestTypeCode);
        final RequestTask requestTask = createRequestTask(requestTaskId, request, appUser.getUserId(),
            "proceTaskId", requestTaskTypeCode, allowedRequestTaskActions);

        final UserDTO requestTaskAssigneeUser = RegulatorUserDTO.builder().firstName("fn").lastName("ln").build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(userServiceDelegator.getUserById(requestTask.getAssignee())).thenReturn(requestTaskAssigneeUser);
        when(requestTaskAuthorizationResourceService.hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria))
            .thenReturn(true);
        when(requestTaskAuthorizationResourceService.hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria))
                .thenReturn(true);
        when(resolver.resolveEligibleRequestTaskActions(requestTask)).thenReturn(allowedRequestTaskActions);


        //invoke
        RequestTaskItemDTO result = service.getTaskItemInfo(requestTaskId, appUser);

        assertThat(result.getRequestTask().getType()).isEqualTo(requestTaskTypeCode);
        assertThat(result.getRequestTask().getDaysRemaining()).isEqualTo(14);
        assertThat(result.getAllowedRequestTaskActions()).containsExactlyInAnyOrderElementsOf(allowedRequestTaskActions);
        assertThat(result.isUserAssignCapable()).isTrue();
        assertThat(result.getRequestTask().getAssigneeUserId()).isEqualTo(user);
        assertThat(result.getRequestInfo().getResources().get(ResourceType.CA)).isEqualTo(ca.name());

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(userServiceDelegator, times(1)).getUserById(requestTask.getAssignee());
        verify(requestTaskAuthorizationResourceService, times(1))
            .hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria);
        verify(requestTaskAuthorizationResourceService, times(1))
                .hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria);
        verifyNoMoreInteractions(requestTaskAuthorizationResourceService);
    }

    @Test
    void getTaskItemInfo_task_action_filtered() {
        final String user = "user";
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final AppUser appUser = AppUser.builder().userId(user).firstName("fn").lastName("ln").roleType(RoleTypeConstants.REGULATOR).build();
        final Long requestTaskId = 1L;
        final String requestTypeCode = "requestTypeCode";
        final String requestTaskTypeCode = "requestTaskTypeCode";
        List<String> allowedRequestTaskActions = List.of("action1", "action2");

        final Request request = createRequest("1", ca, accountId, requestTypeCode);
        final RequestTask requestTask = createRequestTask(requestTaskId, request, appUser.getUserId(),
                "proceTaskId", requestTaskTypeCode, allowedRequestTaskActions);

        final UserDTO requestTaskAssigneeUser = RegulatorUserDTO.builder().firstName("fn").lastName("ln").build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
                .requestResources(Map.of(
                        ResourceType.ACCOUNT, accountId.toString(),
                        ResourceType.CA, ca.name()
                ))
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(userServiceDelegator.getUserById(requestTask.getAssignee())).thenReturn(requestTaskAssigneeUser);
        when(requestTaskAuthorizationResourceService.hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria))
                .thenReturn(true);
        when(requestTaskAuthorizationResourceService.hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria))
                .thenReturn(true);
        when(resolver.resolveEligibleRequestTaskActions(requestTask)).thenReturn(List.of("action1"));


        //invoke
        RequestTaskItemDTO result = service.getTaskItemInfo(requestTaskId, appUser);

        assertThat(result.getRequestTask().getType()).isEqualTo(requestTaskTypeCode);
        assertThat(result.getRequestTask().getDaysRemaining()).isEqualTo(14);
        assertThat(result.getAllowedRequestTaskActions()).containsExactlyInAnyOrderElementsOf(List.of("action1"));
        assertThat(result.isUserAssignCapable()).isTrue();
        assertThat(result.getRequestTask().getAssigneeUserId()).isEqualTo(user);
        assertThat(result.getRequestInfo().getResources().get(ResourceType.CA)).isEqualTo(ca.name());

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(userServiceDelegator, times(1)).getUserById(requestTask.getAssignee());
        verify(requestTaskAuthorizationResourceService, times(1))
                .hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria);
        verify(requestTaskAuthorizationResourceService, times(1))
                .hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria);
        verifyNoMoreInteractions(requestTaskAuthorizationResourceService);
    }

    @Test
    void getTaskItemInfo_assignee_user_has_not_execute_scope_on_request_tasks() {
        final String user = "user";
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final AppUser appUser = AppUser.builder().userId(user).firstName("fn").lastName("ln").roleType(RoleTypeConstants.REGULATOR).build();
        final Long requestTaskId = 1L;
        final String requestTypeCode = "requestTypeCode";
        final String requestTaskTypeCode = "requestTaskTypeCode";

        final Request request = createRequest("1", ca, accountId, requestTypeCode);
        final RequestTask requestTask = createRequestTask(requestTaskId, request, user,
            "proceTaskId", requestTaskTypeCode, Collections.emptyList());

        final UserDTO requestTaskAssigneeUser = RegulatorUserDTO.builder().firstName("fn").lastName("ln").build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(userServiceDelegator.getUserById(requestTask.getAssignee())).thenReturn(requestTaskAssigneeUser);
        when(requestTaskAuthorizationResourceService.hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria))
            .thenReturn(false);
        when(requestTaskAuthorizationResourceService.hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria))
            .thenReturn(false);

        //invoke
        RequestTaskItemDTO result = service.getTaskItemInfo(requestTaskId, appUser);

        assertThat(result.getRequestTask().getType()).isEqualTo(requestTaskTypeCode);
        assertThat(result.getRequestTask().getDaysRemaining()).isEqualTo(14);
        assertThat(result.getAllowedRequestTaskActions()).isEmpty();
        assertThat(result.isUserAssignCapable()).isFalse();
        assertThat(result.getRequestInfo().getResources().get(ResourceType.CA)).isEqualTo(ca.name());

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(userServiceDelegator, times(1)).getUserById(requestTask.getAssignee());
        verify(requestTaskAuthorizationResourceService, times(1))
            .hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria);
        verify(requestTaskAuthorizationResourceService, times(1))
            .hasUserExecuteScopeOnRequestTaskType(appUser, requestTaskTypeCode, resourceCriteria);
    }

    @Test
    void getTaskItemInfo_user_is_not_task_assignee() {
        final String user = "user";
        final Long accountId = 1L;
        final CompetentAuthorityEnum ca = CompetentAuthorityEnum.ENGLAND;
        final AppUser appUser = AppUser.builder().userId(user).firstName("fn").lastName("ln").roleType(RoleTypeConstants.REGULATOR).build();
        final Long requestTaskId = 1L;
        final String requestTypeCode = "requestTypeCode";
        final String requestTaskTypeCode = "requestTaskTypeCode";

        final Request request = createRequest("1", ca, accountId, requestTypeCode);
        final RequestTask requestTask = createRequestTask(requestTaskId, request, "assignee",
            "proceTaskId", requestTaskTypeCode, Collections.emptyList());

        final UserDTO requestTaskAssigneeUser = RegulatorUserDTO.builder().firstName("fn").lastName("ln").build();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
        		.requestResources(Map.of(
                		ResourceType.ACCOUNT, accountId.toString(),
                		ResourceType.CA, ca.name()
                		))
                .build();

        when(requestTaskService.findTaskById(requestTaskId)).thenReturn(requestTask);
        when(userServiceDelegator.getUserById(requestTask.getAssignee())).thenReturn(requestTaskAssigneeUser);
        when(requestTaskAuthorizationResourceService.hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria))
            .thenReturn(true);

        //invoke
        RequestTaskItemDTO result = service.getTaskItemInfo(requestTaskId, appUser);

        assertThat(result.getRequestTask().getType()).isEqualTo(requestTaskTypeCode);
        assertThat(result.getRequestTask().getDaysRemaining()).isEqualTo(14);
        assertThat(result.getAllowedRequestTaskActions()).isEmpty();
        assertThat(result.isUserAssignCapable()).isTrue();
        assertThat(result.getRequestInfo().getResources().get(ResourceType.CA)).isEqualTo(ca.name());

        verify(requestTaskService, times(1)).findTaskById(requestTaskId);
        verify(userServiceDelegator, times(1)).getUserById(requestTask.getAssignee());
        verify(requestTaskAuthorizationResourceService, times(1))
            .hasUserAssignScopeOnRequestTasks(appUser, resourceCriteria);
        verify(requestTaskAuthorizationResourceService, never())
            .hasUserExecuteScopeOnRequestTaskType(any(), anyString(), any());
    }

    private Request createRequest(String requestId, CompetentAuthorityEnum ca,
        Long accountId, String requestTypeCode) {
    	Request request = Request.builder()
            .id(requestId)
            .type(RequestType.builder().code(requestTypeCode).resourceType(ResourceType.ACCOUNT).build())
            .status("inprogress")
            .processInstanceId("procInst")
            .creationDate(LocalDateTime.now())
            .build();
        addResourcesToRequest(accountId, ca, request);
        return request;
    }

    private RequestTask createRequestTask(Long requestTaskId, Request request, String assignee, String processTaskId,
        String requestTaskTypeCode,  List<String> allowedRequestTaskActions) {
        return RequestTask.builder()
            .id(requestTaskId)
            .request(request)
            .processTaskId(processTaskId)
            .type(RequestTaskType.builder().code(requestTaskTypeCode).actionTypes(
            		allowedRequestTaskActions.stream().map(action -> RequestTaskActionType.builder().code(action).build()).collect(Collectors.toSet())
            		).build())
            .assignee(assignee)
            .dueDate(LocalDate.now().plusDays(14))
            .payload(TestRequestTaskPayload.builder().build())
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

    private static class TestRequestTaskActionEligibilityEvaluator implements RequestTaskActionEligibilityEvaluator<TestRequestTaskPayload> {


        @Override
        public boolean isEligible(TestRequestTaskPayload requestTaskPayload) {
            return false;
        }

        @Override
        public String getRequestTaskType() {
            return null;
        }

        @Override
        public List<String> getRequestTaskActionTypes() {
            return null;
        }
    }
}
