package uk.gov.netz.api.workflow.request.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.AuthorizationRulesQueryService;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestTaskService {

    private final RequestTaskRepository requestTaskRepository;
    private final AuthorizationRulesQueryService authorizationRulesQueryService;

    public RequestTask findTaskById(Long id) {
        return requestTaskRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    
    public RequestTask findByTypeAndRequestId(String requestTaskType, String requestId) {
        return requestTaskRepository.findByTypeCodeAndRequestId(requestTaskType, requestId);
    }

    @Transactional
    public void updateRequestTaskPayload(RequestTask requestTask, RequestTaskPayload requestTaskPayload) {
        requestTask.setPayload(requestTaskPayload);
    }

    public List<RequestTask> findTasksByRequestIdAndRoleType(String requestId, String roleType) {
        Set<String> roleAllowedTaskTypes = authorizationRulesQueryService
            .findResourceSubTypesByResourceTypeAndRoleType(ResourceType.REQUEST_TASK, roleType);

        return requestTaskRepository.findByRequestId(requestId).stream()
            .filter(requestTask -> roleAllowedTaskTypes.contains(requestTask.getType().getCode()))
            .collect(Collectors.toList());
    }

    public List<RequestTask> findTasksByTypeInAndAccountId(Set<RequestTaskType> types, Long accountId) {
        return requestTaskRepository.findByTypeInAndRequestAccountId(types, accountId);
    }
}
