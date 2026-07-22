package uk.gov.netz.api.account.search.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.query.AccountSearchResultRow;
import uk.gov.netz.api.account.search.query.AccountSearchResultRowMapper;
import uk.gov.netz.api.account.search.criteria.AccountSearchContactFilter;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.criteria.AccountSearchScope;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;
import uk.gov.netz.api.account.search.query.AccountSearchQueryRepository;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.verifier.VerifierAccountAccessService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSearchQueryServiceImplTest {

    @Mock
    private AccountSearchQueryRepository<Account, AccountSearchResultInfoDTO> repository;

    @Mock
    private AccountSearchEntityPaths<Account> paths;

    @Mock
    private VerifierAccountAccessService verifierAccountAccessService;

    private AccountSearchQueryServiceImpl<Account, AccountSearchResultRow, AccountSearchResultInfoDTO> service;

    @BeforeEach
    void setUp() {
        service = new AccountSearchQueryServiceImpl<>(
                repository,
                paths,
                verifierAccountAccessService,
                new AccountSearchResultRowMapper());
    }

    @Test
    void search_operatorScope_callsRepository() {
        AppUser user = operatorUser(Set.of(1L, 2L));
        AccountSearchFilterCriteria criteria = criteria();
        AccountSearchResults<AccountSearchResultInfoDTO> expected = sampleResults();

        when(repository.search(eq(criteria), any(AccountSearchQueryContext.class), eq(paths)))
                .thenReturn(expected);

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        ArgumentCaptor<AccountSearchQueryContext> contextCaptor =
                ArgumentCaptor.forClass(AccountSearchQueryContext.class);
        verify(repository).search(eq(criteria), contextCaptor.capture(), eq(paths));
        verifyNoInteractions(verifierAccountAccessService);

        AccountSearchQueryContext context = contextCaptor.getValue();
        assertThat(context.getScope()).isEqualTo(AccountSearchScope.ACCOUNT_IDS);
        assertThat(context.getRoleAccountIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(context.isContactFilterActive()).isFalse();
        assertThat(results).isSameAs(expected);
    }

    @Test
    void search_regulatorScope_callsRepository() {
        AppUser user = regulatorUser(CompetentAuthorityEnum.ENGLAND);
        AccountSearchFilterCriteria criteria = criteria();
        AccountSearchResults<AccountSearchResultInfoDTO> expected = sampleResults();

        when(repository.search(eq(criteria), any(AccountSearchQueryContext.class), eq(paths)))
                .thenReturn(expected);

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        ArgumentCaptor<AccountSearchQueryContext> contextCaptor =
                ArgumentCaptor.forClass(AccountSearchQueryContext.class);
        verify(repository).search(eq(criteria), contextCaptor.capture(), eq(paths));
        verifyNoInteractions(verifierAccountAccessService);

        AccountSearchQueryContext context = contextCaptor.getValue();
        assertThat(context.getScope()).isEqualTo(AccountSearchScope.COMPETENT_AUTHORITY);
        assertThat(context.getCompetentAuthority()).isEqualTo(CompetentAuthorityEnum.ENGLAND);
        assertThat(context.isContactFilterActive()).isFalse();
        assertThat(results).isSameAs(expected);
    }

    @Test
    void search_verifierScope_callsVerifierAccountAccessService() {
        AppUser user = verifierUser();
        Set<Long> authorizedAccountIds = Set.of(10L, 20L);
        AccountSearchFilterCriteria criteria = criteria();
        AccountSearchResults<AccountSearchResultInfoDTO> expected = sampleResults();

        when(verifierAccountAccessService.findAuthorizedAccountIds(user)).thenReturn(authorizedAccountIds);
        when(repository.search(eq(criteria), any(AccountSearchQueryContext.class), eq(paths)))
                .thenReturn(expected);

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        verify(verifierAccountAccessService).findAuthorizedAccountIds(user);

        ArgumentCaptor<AccountSearchQueryContext> contextCaptor =
                ArgumentCaptor.forClass(AccountSearchQueryContext.class);
        verify(repository).search(eq(criteria), contextCaptor.capture(), eq(paths));

        AccountSearchQueryContext context = contextCaptor.getValue();
        assertThat(context.getScope()).isEqualTo(AccountSearchScope.ACCOUNT_IDS);
        assertThat(context.getRoleAccountIds()).containsExactlyInAnyOrder(10L, 20L);
        assertThat(results).isSameAs(expected);
    }

    @Test
    void search_emptyOperatorAccounts_returnsEmptyAndDoesNotCallRepository() {
        AppUser user = operatorUser(Set.of());
        AccountSearchFilterCriteria criteria = criteria();

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        assertThat(results).isEqualTo(AccountSearchResults.emptyAccountSearchResults());
        verifyNoInteractions(repository, verifierAccountAccessService);
    }

    @Test
    void search_emptyVerifierIds_returnsEmptyAndDoesNotCallRepository() {
        AppUser user = verifierUser();
        AccountSearchFilterCriteria criteria = criteria();

        when(verifierAccountAccessService.findAuthorizedAccountIds(user)).thenReturn(Set.of());

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        assertThat(results).isEqualTo(AccountSearchResults.emptyAccountSearchResults());
        verify(verifierAccountAccessService).findAuthorizedAccountIds(user);
        verify(repository, never()).search(any(), any(), any());
    }

    @Test
    void search_nullRegulatorCa_returnsEmptyAndDoesNotCallRepository() {
        AppUser user = AppUser.builder()
                .roleType(RoleTypeConstants.REGULATOR)
                .authorities(List.of())
                .build();
        AccountSearchFilterCriteria criteria = criteria();

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.none());

        assertThat(results).isEqualTo(AccountSearchResults.emptyAccountSearchResults());
        verifyNoInteractions(repository, verifierAccountAccessService);
    }

    @Test
    void search_activeContactFilterWithIds_callsRepository() {
        AppUser user = operatorUser(Set.of(1L, 2L));
        AccountSearchFilterCriteria criteria = criteria();
        Set<Long> contactAccountIds = Set.of(2L);
        AccountSearchResults<AccountSearchResultInfoDTO> expected = sampleResults();

        when(repository.search(eq(criteria), any(AccountSearchQueryContext.class), eq(paths)))
                .thenReturn(expected);

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.of(contactAccountIds));

        ArgumentCaptor<AccountSearchQueryContext> contextCaptor =
                ArgumentCaptor.forClass(AccountSearchQueryContext.class);
        verify(repository).search(eq(criteria), contextCaptor.capture(), eq(paths));

        AccountSearchQueryContext context = contextCaptor.getValue();
        assertThat(context.isContactFilterActive()).isTrue();
        assertThat(context.getContactAccountIds()).containsExactly(2L);
        assertThat(results).isSameAs(expected);
    }

    @Test
    void search_activeContactFilterWithEmptyIds_returnsEmptyAndDoesNotCallRepository() {
        AppUser user = operatorUser(Set.of(1L, 2L));
        AccountSearchFilterCriteria criteria = criteria();

        AccountSearchResults<AccountSearchResultInfoDTO> results =
                service.search(user, criteria, AccountSearchContactFilter.of(Set.of()));

        assertThat(results).isEqualTo(AccountSearchResults.emptyAccountSearchResults());
        verify(repository, never()).search(any(), any(), any());
    }

    @Test
    void search_unsupportedRole_throwsUnsupportedOperationException() {
        AppUser user = AppUser.builder().roleType("ANOTHER_ROLE").build();
        AccountSearchFilterCriteria criteria = criteria();

        assertThatThrownBy(() -> service.search(user, criteria, AccountSearchContactFilter.none()))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("Fetching accounts for role type ANOTHER_ROLE is not supported");

        verifyNoInteractions(repository, verifierAccountAccessService);
    }

    private static AppUser operatorUser(Set<Long> accountIds) {
        List<AppAuthority> authorities = accountIds.stream()
                .map(id -> AppAuthority.builder()
                        .accountId(id)
                        .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                        .build())
                .collect(Collectors.toList());
        return AppUser.builder()
                .roleType(RoleTypeConstants.OPERATOR)
                .authorities(authorities)
                .build();
    }

    private static AppUser regulatorUser(CompetentAuthorityEnum competentAuthority) {
        return AppUser.builder()
                .roleType(RoleTypeConstants.REGULATOR)
                .authorities(List.of(AppAuthority.builder()
                        .competentAuthority(competentAuthority)
                        .build()))
                .build();
    }

    private static AppUser verifierUser() {
        return AppUser.builder()
                .roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder()
                        .verificationBodyId(1L)
                        .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                        .build()))
                .build();
    }

    private static AccountSearchFilterCriteria criteria() {
        return AccountSearchFilterCriteria.builder()
                .paging(PagingRequest.builder().pageNumber(0).pageSize(20).build())
                .build();
    }

    private static AccountSearchResults<AccountSearchResultInfoDTO> sampleResults() {
        return AccountSearchResults.<AccountSearchResultInfoDTO>builder()
                .accounts(List.of(new AccountSearchResultInfoDTO(1L, "Alpha", "buss1", null)))
                .total(1L)
                .build();
    }
}
