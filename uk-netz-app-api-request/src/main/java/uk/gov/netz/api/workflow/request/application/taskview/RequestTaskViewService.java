package uk.gov.netz.api.workflow.request.application.taskview;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.user.application.UserServiceDelegator;
import uk.gov.netz.api.user.core.domain.dto.UserDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class RequestTaskViewService {

    private final RequestTaskService requestTaskService;
    private final UserServiceDelegator userServiceDelegator;
    private final RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;
    private final RequestTaskEligibleActionsResolver resolver;

    private static final RequestTaskMapper requestTaskMapper = Mappers.getMapper(RequestTaskMapper.class);
    private static final RequestInfoMapper requestInfoMapper = Mappers.getMapper(RequestInfoMapper.class);

    @Transactional
    public RequestTaskItemDTO getTaskItemInfo(Long taskId, AppUser currentUser) {
        RequestTask requestTask = requestTaskService.findTaskById(taskId);

        return RequestTaskItemDTO.builder()
                .requestTask(buildTaskDTO(requestTask))
                .allowedRequestTaskActions(buildAllowedRequestTaskActions(currentUser, requestTask))
                .userAssignCapable(isUserCapableToAssignRequestTask(currentUser, requestTask))
                .requestInfo(requestInfoMapper.toRequestInfoDTO(requestTask.getRequest()))
                .build();
    }

    @Transactional(readOnly = true)
    public Set<String> getRequestTaskTypes(String roleType) {
        return requestTaskAuthorizationResourceService.findRequestTaskTypesByRoleType(roleType);
    }

    private RequestTaskDTO buildTaskDTO(RequestTask requestTask) {
        UserDTO assigneeUser = !ObjectUtils.isEmpty(requestTask.getAssignee())
                ? userServiceDelegator.getUserById(requestTask.getAssignee())
                : null;

        return requestTaskMapper.toTaskDTO(requestTask, assigneeUser);
    }

    private List<String> buildAllowedRequestTaskActions(AppUser currentUser, RequestTask requestTask) {
        if (isUserCapableToExecuteRequestTask(currentUser, requestTask)) {
            return resolver.resolveEligibleRequestTaskActions(requestTask);
        } else {
            return Collections.emptyList();
        }
    }

    private boolean isUserCapableToExecuteRequestTask(AppUser user, RequestTask requestTask) {
        return user.getUserId().equals(requestTask.getAssignee()) && hasUserExecuteScopeOnRequestTask(user, requestTask);
    }

    private boolean isUserCapableToAssignRequestTask(AppUser user, RequestTask requestTask) {
        return requestTaskAuthorizationResourceService.hasUserAssignScopeOnRequestTasks(user, buildResourceCriteria(requestTask));
    }

    private boolean hasUserExecuteScopeOnRequestTask(AppUser user, RequestTask requestTask) {
        return requestTaskAuthorizationResourceService.hasUserExecuteScopeOnRequestTaskType(user, requestTask.getType().getCode(),
                buildResourceCriteria(requestTask));
    }

    private ResourceCriteria buildResourceCriteria(RequestTask requestTask) {
        Request request = requestTask.getRequest();
        return ResourceCriteria.builder()
                .requestResources(request.getRequestResourcesMap())
                .build();
    }
}
