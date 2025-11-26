package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperatorRequestTaskRoleTypeAuthorizationQueryService implements RequestTaskRoleTypeAuthorizationQueryService {
    private final OperatorAuthorityResourceService operatorAuthorityResourceService;

    @Override
    public List<String> findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(String requestTaskType,
                                                                                ResourceCriteria resourceCriteria,
                                                                                boolean requiresPermission) {
        if (!requiresPermission) {
            return operatorAuthorityResourceService.findUsersByAccountId(resourceCriteria.getAccountId());
        } else {
            return operatorAuthorityResourceService.
                    findUsersWithScopeOnResourceTypeAndSubTypeAndAccountId(
                    ResourceType.REQUEST_TASK, requestTaskType, Scope.REQUEST_TASK_EXECUTE, resourceCriteria.getAccountId());
        }
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.OPERATOR;
    }
}
