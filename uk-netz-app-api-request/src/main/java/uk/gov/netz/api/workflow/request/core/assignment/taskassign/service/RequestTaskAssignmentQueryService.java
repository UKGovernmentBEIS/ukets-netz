package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.userinfoapi.UserInfo;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.dto.AssigneeUserInfoDTO;
import uk.gov.netz.api.workflow.request.core.assignment.taskassign.transform.AssigneeUserInfoMapper;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskTypeService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for performing query assignments on {@link RequestTask} objects.
 */
@Service
@RequiredArgsConstructor
public class RequestTaskAssignmentQueryService {

    private final RequestTaskService requestTaskService;
    private final RequestTaskTypeService requestTaskTypeService;
    private final UserInfoApi userInfoApi;
    private final RequestTaskAssignmentValidationService requestTaskAssignmentValidationService;
    private final RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;
    private final AssigneeUserInfoMapper assigneeUserInfoMapper = Mappers.getMapper(AssigneeUserInfoMapper.class);

    /**
     * Retrieves all user that have the authority to be assigned to the provided task id.
     * @param taskId the task Id
     * @param authenticatedUser the user {@link AppUser} that performs the action
     * @return {@link List} of {@link UserInfo}
     */
    @Transactional(readOnly = true)
    public List<AssigneeUserInfoDTO> getCandidateAssigneesByTaskId(Long taskId, AppUser authenticatedUser) {
        RequestTask requestTask = requestTaskService.findTaskById(taskId);

        requestTaskAssignmentValidationService.validateTaskAssignmentCapability(requestTask);
        
        ResourceCriteria resourceCriteria = 
        		ResourceCriteria.builder()
                .requestResources(requestTask.getRequest().getRequestResourcesMap())
                .build();

        List<String> candidateAssignees = requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
            requestTask.getType().getCode(),
            resourceCriteria,
            authenticatedUser.getRoleType());

        if (requestTask.getType().isPeerReview()) {
            candidateAssignees.remove(requestTask.getRequest().getPayload().getRegulatorReviewer());
        }

        return getCandidateAssigneesUserInfo(candidateAssignees);
    }

    @Transactional(readOnly = true)
    public List<AssigneeUserInfoDTO> getCandidateAssigneesByTaskType(Long currentTaskId, String taskTypeToFindCandidates, AppUser authenticatedUser) {
    	final RequestTaskType requestTaskTypeToFindCandidates = requestTaskTypeService.findByCode(taskTypeToFindCandidates);
    	requestTaskAssignmentValidationService.validateTaskAssignmentCapability(requestTaskTypeToFindCandidates);

    	RequestTask requestTask = requestTaskService.findTaskById(currentTaskId);
        Request request = requestTask.getRequest();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
                .requestResources(request.getRequestResourcesMap())
                .build();

        List<String> candidateAssignees = requestTaskAuthorizationResourceService.findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
        		requestTaskTypeToFindCandidates.getCode(),
            resourceCriteria,
            authenticatedUser.getRoleType());

        if (requestTaskTypeToFindCandidates.isPeerReview()) {
            candidateAssignees.remove(authenticatedUser.getUserId());
        }

        return getCandidateAssigneesUserInfo(candidateAssignees);
    }

    private List<AssigneeUserInfoDTO> getCandidateAssigneesUserInfo(List<String> candidateAssignees) {
        return userInfoApi.getUsers(candidateAssignees).stream()
            .map(assigneeUserInfoMapper::toAssigneeUserInfoDTO)
            .collect(Collectors.toList());
    }
}
