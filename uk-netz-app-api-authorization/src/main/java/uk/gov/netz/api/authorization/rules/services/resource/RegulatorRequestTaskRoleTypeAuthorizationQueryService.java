package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegulatorRequestTaskRoleTypeAuthorizationQueryService implements RequestTaskRoleTypeAuthorizationQueryService {
    private final RegulatorAuthorityResourceService regulatorAuthorityResourceService;

    @Override
    public List<String> findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(String requestTaskType,
                                                                                ResourceCriteria resourceCriteria,
                                                                                boolean requiresPermission) {
        if (!requiresPermission) {
            return regulatorAuthorityResourceService.findUsersByCompetentAuthority(resourceCriteria.getCompetentAuthority());
        } else {
            return regulatorAuthorityResourceService.
                    findUsersWithScopeOnResourceTypeAndSubTypeAndCA(
                    ResourceType.REQUEST_TASK, requestTaskType, Scope.REQUEST_TASK_EXECUTE, resourceCriteria.getCompetentAuthority());
        }
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.REGULATOR;
    }
}
