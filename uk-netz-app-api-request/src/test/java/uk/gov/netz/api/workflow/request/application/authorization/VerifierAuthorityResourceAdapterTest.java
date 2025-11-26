package uk.gov.netz.api.workflow.request.application.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.core.domain.Permission;
import uk.gov.netz.api.authorization.rules.services.resource.VerifierAuthorityResourceService;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifierAuthorityResourceAdapterTest {

    @InjectMocks
    private VerifierAuthorityResourceAdapter verifierAuthorityResourceAdapter;

    @Mock
    private VerifierAuthorityResourceService verifierAuthorityResourceService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RequestTaskRepository taskRepository;


    @Test
    void getUserScopedRequestTaskTypesVerifierAdmin() {
        final String userId = "userId";
        final AppUser user = AppUser.builder().userId(userId)
                .authorities(List.of(AppAuthority.builder()
                        .permissions(List.of(Permission.PERM_VB_ACCESS_ALL_ACCOUNTS)).build()))
                .build();
        final Long vbId = 1L;

        String requestTaskType1 = "requestTaskType1";
        String requestTaskType2 = "requestTaskType2";

        when(accountRepository.findAllIdsByVerificationBody(vbId)).thenReturn(List.of(3L, 4L));
        when(verifierAuthorityResourceService.findUserScopedRequestTaskTypes(userId))
                .thenReturn(Map.of(1L, Set.of(requestTaskType1, requestTaskType2)));

        Map<Long, Set<String>> userScopedRequestTaskTypes =
                verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(user);

        assertThat(userScopedRequestTaskTypes).containsExactlyInAnyOrderEntriesOf(
                Map.of(3L, Set.of(requestTaskType1, requestTaskType2),
                        4L, Set.of(requestTaskType1, requestTaskType2))
        );
    }

    @Test
    void getUserScopedRequestTaskTypesVerifier() {
        final String userId = "userId";
        final AppUser user = AppUser.builder().userId(userId)
                .authorities(List.of(AppAuthority.builder()
                        .permissions(List.of()).build()))
                .build();

        final Long vbId = 1L;
        Account acc2 = TestAccount.builder().id(2L).build();
        Account acc3 = TestAccount.builder().id(3L).build();
        Account acc_vb_site = TestAccount.builder().id(4L).build();

        String requestTaskType1 = "requestTaskType1";
        String requestTaskType2 = "requestTaskType2";

        final Set<String> taskTypesString = Set.of(requestTaskType1, requestTaskType2);
        final Set<String> taskTypes = Set.of(requestTaskType1, requestTaskType2);

        when(verifierAuthorityResourceService.findUserScopedRequestTaskTypes(userId))
                .thenReturn(Map.of(
                        vbId,
                        taskTypesString
                ));
        when(taskRepository.findAccountIdsByTaskAssigneeAndTaskTypeInAndVerificationBody( "userId", taskTypes, vbId))
                .thenReturn(List.of(acc2.getId(), acc3.getId()));
        when(accountRepository.findAccountsByContactTypeAndUserId(AccountContactType.VB_SITE, "userId"))
            .thenReturn(List.of(acc_vb_site));

        Map<Long, Set<String>> userScopedRequestTaskTypes =
                verifierAuthorityResourceAdapter.getUserScopedRequestTaskTypes(user);

        assertThat(userScopedRequestTaskTypes).containsExactlyInAnyOrderEntriesOf(
            Map.of(acc3.getId(), Set.of(requestTaskType1, requestTaskType2),
                acc2.getId(), Set.of(requestTaskType1, requestTaskType2),
                acc_vb_site.getId(), Set.of(requestTaskType1, requestTaskType2))
        );
    }
}