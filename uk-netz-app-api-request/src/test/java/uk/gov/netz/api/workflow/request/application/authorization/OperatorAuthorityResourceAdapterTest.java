package uk.gov.netz.api.workflow.request.application.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.resource.OperatorAuthorityResourceService;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperatorAuthorityResourceAdapterTest {

    @InjectMocks
    private OperatorAuthorityResourceAdapter operatorAuthorityResourceAdapter;

    @Mock
    private OperatorAuthorityResourceService operatorAuthorityResourceService;

    @Mock
    private AccountQueryService accountQueryService;

    @Test
    void getUserScopedRequestTaskTypesByAccountId() {
        final String userId = "userId";
        final Long accountId = 1L;

        when(operatorAuthorityResourceService.findUserScopedRequestTaskTypesByAccounts(userId, Set.of(accountId)))
            .thenReturn(Map.of(
                accountId, Set.of("taskType1", "taskType2"))
            );

        Map<Long, Set<String>> userScopedRequestTaskTypesByAccounts =
            operatorAuthorityResourceAdapter.getUserScopedRequestTaskTypesByAccountId(userId, accountId);

        assertThat(userScopedRequestTaskTypesByAccounts).containsExactlyEntriesOf(Map.of(
            accountId,
            Set.of("taskType1", "taskType2"))
        );

        verify(operatorAuthorityResourceService, times(1))
            .findUserScopedRequestTaskTypesByAccounts(userId, Set.of(accountId));
        verifyNoInteractions(accountQueryService);
    }

    @Test
    void getUserScopedRequestTaskTypes() {
        final Long accountId1 = 1L;
        final Long accountId2 = 2L;
        final List<Long> accounts = List.of(accountId1, accountId2);
        final String userId = "userId";
        final AppUser appUser = AppUser.builder()
            .userId(userId)
            .authorities(List.of(
                AppAuthority.builder().accountId(accountId1).build(),
                AppAuthority.builder().accountId(accountId2).build()
                )
            )
            .build();
        final Set<Long> installationAccounts = Set.of(accountId1, accountId2);

        when(accountQueryService.getAccountIds(accounts)).thenReturn(installationAccounts);
        when(operatorAuthorityResourceService.findUserScopedRequestTaskTypesByAccounts(userId, installationAccounts))
            .thenReturn(Map.of(
                accountId1, Set.of("taskType1")
                )
            );

        Map<Long, Set<String>> userScopedRequestTaskTypes =
            operatorAuthorityResourceAdapter.getUserScopedRequestTaskTypes(appUser);

        assertThat(userScopedRequestTaskTypes).containsExactlyEntriesOf(
            Map.of(accountId1, Set.of("taskType1"))
        );

        verify(accountQueryService, times(1)).getAccountIds(accounts);
        verify(operatorAuthorityResourceService, times(1))
            .findUserScopedRequestTaskTypesByAccounts(userId, installationAccounts);
    }
}