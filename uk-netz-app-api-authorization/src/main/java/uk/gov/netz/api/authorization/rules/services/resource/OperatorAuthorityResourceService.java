package uk.gov.netz.api.authorization.rules.services.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.authorization.core.repository.AuthorityRepository;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.authorization.rules.domain.Scope;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OperatorAuthorityResourceService {

    private final AuthorityRepository authorityRepository;

    public List<String> findUsersWithScopeOnResourceTypeAndSubTypeAndAccountId(
            String resourceType, String resourceSubType, String scope, Long accountId) {
        return authorityRepository.findOperatorUsersWithScopeOnResourceTypeAndResourceSubTypeAndAccountId(
                resourceType, resourceSubType, scope, accountId);
    }
    
    public List<String> findUsersByAccountId(Long accountId) {
        return authorityRepository.findOperatorUsersByAccountId(accountId);
    }

    public Map<Long, Set<String>> findUserScopedRequestTaskTypesByAccounts(String userId, Set<Long> accounts) {
        return authorityRepository
            .findResourceSubTypesOperatorUserHasScopeByAccounts(userId, accounts, ResourceType.REQUEST_TASK, Scope.REQUEST_TASK_VIEW);
    }
}
