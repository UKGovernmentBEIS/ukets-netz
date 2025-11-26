package uk.gov.netz.api.workflow.request.flow.common.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.services.resource.RequestTaskAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.ResourceCriteria;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PeerReviewerTaskAssignmentValidator {
    private final RequestTaskAuthorizationResourceService requestTaskAuthorizationResourceService;

    public void validate(RequestTask requestTask, RequestTaskType requestTaskTypeToBeAssigned, String selectedAssignee, AppUser appUser) {
        if (!hasUserPermissionsToBeAssignedToTaskType(requestTask, requestTaskTypeToBeAssigned, selectedAssignee, appUser)) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_ALLOWED);
        }
    }

    private boolean hasUserPermissionsToBeAssignedToTaskType(RequestTask currentRequestTask,
                                                            RequestTaskType requestTaskTypeToBeAssigned, String userId, AppUser appUser) {
        Request request = currentRequestTask.getRequest();

        ResourceCriteria resourceCriteria = ResourceCriteria.builder()
                .requestResources(request.getRequestResourcesMap())
                .build();

        List<String> candidateAssignees = requestTaskAuthorizationResourceService
                .findUsersWhoCanExecuteRequestTaskTypeByAccountCriteriaAndRoleType(
                        requestTaskTypeToBeAssigned.getCode(), resourceCriteria, appUser.getRoleType());

        if (requestTaskTypeToBeAssigned.isPeerReview()) {
            candidateAssignees.remove(appUser.getUserId());
        }

        return candidateAssignees.contains(userId);
    }
}
