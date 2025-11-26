package uk.gov.netz.api.authorization.rules.services.resource;

import java.util.List;

public interface RequestTaskRoleTypeAuthorizationQueryService {
    List<String> findUsersWhoCanExecuteRequestTaskTypeByResourceCriteria(String requestTaskType, ResourceCriteria resourceCriteria, boolean requiresPermission);

    String getRoleType();
}
