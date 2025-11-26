package uk.gov.netz.api.workflow.request.application.authorization;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.services.authorization.verifier.VerifierAccountAccessService;
import uk.gov.netz.api.authorization.rules.services.resource.VerifierAuthorityResourceService;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VerifierAuthorityResourceAdapter implements VerifierAccountAccessService {

    private final VerifierAuthorityResourceService verifierAuthorityResourceService;
    private final AccountRepository accountRepository;
    private final RequestTaskRepository taskRepository;

    @Override
    public Set<Long> findAuthorizedAccountIds(final AppUser user) {
        return this.getUserScopedRequestTaskTypes(user).keySet();
    }

    public Map<Long, Set<String>> getUserScopedRequestTaskTypes(final AppUser user) {
        final Map<Long, Set<String>> requestTaskTypesPerVbId =
                verifierAuthorityResourceService.findUserScopedRequestTaskTypes(user.getUserId());

        final boolean hasAccessToAllAccounts = this.hasUserPermissionToAccessAllAccounts(user);
        final Map<Long, Set<String>> requestTaskTypesPerAccount = new HashMap<>();

        for (final Map.Entry<Long, Set<String>> entry : requestTaskTypesPerVbId.entrySet()) {
            final Long vbId = entry.getKey();
            final Set<String> taskTypes = entry.getValue();
            final List<Long> accountIds = hasAccessToAllAccounts ?
                    accountRepository.findAllIdsByVerificationBody(vbId) :
                    (Stream.concat(taskRepository.findAccountIdsByTaskAssigneeAndTaskTypeInAndVerificationBody(user.getUserId(), taskTypes, vbId).stream(),
                    accountRepository.findAccountsByContactTypeAndUserId(AccountContactType.VB_SITE, user.getUserId()).stream().map(Account::getId)).toList());
            accountIds.forEach(accId -> requestTaskTypesPerAccount.put(accId, taskTypes));
        }
        return requestTaskTypesPerAccount;
    }

    private boolean hasUserPermissionToAccessAllAccounts(final AppUser user) {

        return user.getAuthorities().stream()
                .filter(Objects::nonNull)
                .flatMap(authority -> authority.getPermissions().stream())
                .toList()
                .contains(Permission.PERM_VB_ACCESS_ALL_ACCOUNTS);
    }
}