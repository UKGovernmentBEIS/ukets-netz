package uk.gov.netz.api.workflow.request.application.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.resource.OperatorAuthorityResourceService;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

//TODO: This could be implemented with an interface per user role type
@Service
@RequiredArgsConstructor
public class OperatorAuthorityResourceAdapter {
    private final OperatorAuthorityResourceService operatorAuthorityResourceService;
    private final AccountQueryService accountQueryService;

    public Map<Long, Set<String>> getUserScopedRequestTaskTypesByAccountId(String userId, Long accountId) {
        return findUserScopedRequestTaskTypesByAccounts(userId, Set.of(accountId));
    }

    public Map<Long, Set<String>> getUserScopedRequestTaskTypes(AppUser user) {
        Set<Long> accountIds = accountQueryService.getAccountIds(new ArrayList<>(user.getAccounts()));
        return findUserScopedRequestTaskTypesByAccounts(user.getUserId(), accountIds);
    }

    private Map<Long, Set<String>> findUserScopedRequestTaskTypesByAccounts(String userId, Set<Long> accounts) {
        return operatorAuthorityResourceService
            .findUserScopedRequestTaskTypesByAccounts(userId, accounts);
    }
}
