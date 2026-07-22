package uk.gov.netz.api.account.search.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.criteria.AccountSearchCommonSortField;
import uk.gov.netz.api.account.search.paths.QTestAccountSearchEntityPaths;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class AccountSearchQueryRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private EntityManager entityManager;

    private AccountSearchQueryRepository<TestAccount, AccountSearchResultInfoDTO> repository;
    private QTestAccountSearchEntityPaths paths;

    @BeforeEach
    void setUp() {
        repository = AccountSearchQueryRepositoryImpl.forSharedResults(entityManager);
        paths = new QTestAccountSearchEntityPaths();
    }

    @Test
    void search_delegatesProjectionAndResultMappingToProvidedMappers() {
        AccountSearchResultRowProjectionMapper<TestAccount> projectionMapper =
                spy(new AccountSearchResultRowProjectionMapper<>());
        AccountSearchResultRowMapper resultRowMapper = spy(new AccountSearchResultRowMapper());
        repository = new AccountSearchQueryRepositoryImpl<>(
                entityManager,
                new AccountSearchSortMapper<>(),
                projectionMapper,
                resultRowMapper);

        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        flushAndClear();

        AccountSearchFilterCriteria criteria = criteriaBuilder().build();
        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteria,
                AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false),
                paths);

        verify(projectionMapper).constructorProjection(eq(paths));
        verify(resultRowMapper).toResults(any(), eq(1L));
        assertThat(results.getTotal()).isEqualTo(1L);
    }

    @Test
    void search_delegatesSortingToProvidedSortMapper() {
        AccountSearchSortMapper<TestAccount> sortMapper = spy(new AccountSearchSortMapper<>());
        repository = new AccountSearchQueryRepositoryImpl<>(
                entityManager,
                sortMapper,
                new AccountSearchResultRowProjectionMapper<>(),
                new AccountSearchResultRowMapper());

        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        flushAndClear();

        AccountSearchFilterCriteria criteria = criteriaBuilder().build();
        repository.search(
                criteria,
                AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false),
                paths);

        verify(sortMapper).fromCriteria(eq(criteria), eq(paths));
    }

    @Test
    void search_withoutTerm_returnsAllScopedAccountsWithoutKeywordFilter() {
        Account inScopeWithKeyword = createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(inScopeWithKeyword.getId(), "key1", "unique_keyword_alpha");

        Account inScopeWithoutKeyword = createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "gamma", CompetentAuthorityEnum.ENGLAND, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().term(null).build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(3L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId)
                .containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void search_withTerm_filtersByKeywordJoin() {
        Account matching = createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(matching.getId(), "key1", "Operator_NEW");

        Account nonMatching = createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(nonMatching.getId(), "key2", "Installation_OLD");

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().term("new").build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactly(1L);
    }

    @Test
    void search_withTerm_filtersByKeywordJoin_caseInsensitiveUppercaseTerm() {
        Account matching = createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(matching.getId(), "key1", "Operator_NEW");

        Account nonMatching = createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(nonMatching.getId(), "key2", "Installation_OLD");

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().term("NEW").build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactly(1L);
    }

    @Test
    void search_withTerm_partialCaseInsensitiveMatch() {
        Account matching = createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(matching.getId(), "BUSINESS_ID", "ABC123");

        Account nonMatching = createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(nonMatching.getId(), "BUSINESS_ID", "XYZ999");

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().term("abc").build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactly(1L);
    }

    @Test
    void search_withTerm_findsAccountByRegistryIdKeyword() {
        Account matching = createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(matching.getId(), "REGISTRY_ID", "REG-98765");

        Account nonMatching = createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccountSearchAdditionalKeyword(nonMatching.getId(), "ACCOUNT_NAME", "other-operator");

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().term("98765").build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactly(1L);
    }

    @Test
    void search_byAccountIdsScope() {
        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "gamma", CompetentAuthorityEnum.WALES, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 3L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(2L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void search_byCompetentAuthorityScope() {
        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "gamma", CompetentAuthorityEnum.WALES, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().build(),
                AccountSearchQueryContext.forCompetentAuthority(CompetentAuthorityEnum.ENGLAND, Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(2L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void search_byStatusFilter() {
        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY2);
        createAccount(3L, "gamma", CompetentAuthorityEnum.ENGLAND, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().statuses(Set.of(TestAccountStatus.DUMMY2)).build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L), Set.of(), false),
                paths);

        assertThat(results.getTotal()).isEqualTo(1L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactly(2L);
    }

    @Test
    void search_byContactAccountIdsFilter() {
        createAccount(1L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "beta", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "gamma", CompetentAuthorityEnum.ENGLAND, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder().build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L), Set.of(2L, 3L), true),
                paths);

        assertThat(results.getTotal()).isEqualTo(2L);
        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getId).containsExactlyInAnyOrder(2L, 3L);
    }

    @Test
    void search_sortsByOperatorNameDescending() {
        createAccount(1L, "charlie", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "alpha", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "bravo", CompetentAuthorityEnum.ENGLAND, "buss3", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> results = repository.search(
                criteriaBuilder()
                        .sortField(AccountSearchCommonSortField.OPERATOR_NAME)
                        .sortDirection(Sort.Direction.DESC)
                        .build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L), Set.of(), false),
                paths);

        assertThat(results.getAccounts()).extracting(AccountSearchResultInfoDTO::getName)
                .containsExactly("charlie", "bravo", "alpha");
    }

    @Test
    void search_paginationAndCount() {
        createAccount(1L, "a1", CompetentAuthorityEnum.ENGLAND, "buss1", TestAccountStatus.DUMMY);
        createAccount(2L, "a2", CompetentAuthorityEnum.ENGLAND, "buss2", TestAccountStatus.DUMMY);
        createAccount(3L, "a3", CompetentAuthorityEnum.ENGLAND, "buss3", TestAccountStatus.DUMMY);
        createAccount(4L, "a4", CompetentAuthorityEnum.ENGLAND, "buss4", TestAccountStatus.DUMMY);
        createAccount(5L, "a5", CompetentAuthorityEnum.ENGLAND, "buss5", TestAccountStatus.DUMMY);

        flushAndClear();

        AccountSearchResults<AccountSearchResultInfoDTO> firstPage = repository.search(
                criteriaBuilder()
                        .paging(PagingRequest.builder().pageNumber(0).pageSize(2).build())
                        .sortField(AccountSearchCommonSortField.OPERATOR_NAME)
                        .sortDirection(Sort.Direction.ASC)
                        .build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L, 4L, 5L), Set.of(), false),
                paths);

        AccountSearchResults<AccountSearchResultInfoDTO> secondPage = repository.search(
                criteriaBuilder()
                        .paging(PagingRequest.builder().pageNumber(1).pageSize(2).build())
                        .sortField(AccountSearchCommonSortField.OPERATOR_NAME)
                        .sortDirection(Sort.Direction.ASC)
                        .build(),
                AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L, 3L, 4L, 5L), Set.of(), false),
                paths);

        assertThat(firstPage.getTotal()).isEqualTo(5L);
        assertThat(firstPage.getAccounts()).hasSize(2);
        assertThat(firstPage.getAccounts()).extracting(AccountSearchResultInfoDTO::getName)
                .containsExactly("a1", "a2");

        assertThat(secondPage.getTotal()).isEqualTo(5L);
        assertThat(secondPage.getAccounts()).hasSize(2);
        assertThat(secondPage.getAccounts()).extracting(AccountSearchResultInfoDTO::getName)
                .containsExactly("a3", "a4");
    }

    private AccountSearchFilterCriteria.AccountSearchFilterCriteriaBuilder criteriaBuilder() {
        return AccountSearchFilterCriteria.builder()
                .paging(PagingRequest.builder().pageNumber(0).pageSize(20).build());
    }

    private AccountSearchAdditionalKeyword createAccountSearchAdditionalKeyword(Long accountId, String key, String val) {
        AccountSearchAdditionalKeyword keyword = AccountSearchAdditionalKeyword.builder()
                .accountId(accountId)
                .key(key)
                .value(val)
                .build();
        entityManager.persist(keyword);
        return keyword;
    }

    private Account createAccount(
            Long id,
            String accountName,
            CompetentAuthorityEnum ca,
            String businessId,
            TestAccountStatus status) {
        Account account = TestAccount.builder()
                .id(id)
                .competentAuthority(ca)
                .verificationBodyId(1L)
                .status(status)
                .emissionTradingScheme(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME)
                .name(accountName)
                .businessId(businessId)
                .build();
        account.getContacts().put(AccountContactType.PRIMARY, "contact1");
        entityManager.persist(account);
        return account;
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
