package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.QAccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountSearchPredicateBuilderTest {

    private static final QAccountSearchAdditionalKeyword KEYWORD =
            QAccountSearchAdditionalKeyword.accountSearchAdditionalKeyword;

    private final AccountSearchPredicateBuilder<uk.gov.netz.api.account.domain.Account> builder =
            new AccountSearchPredicateBuilder<>();

    private QAccountTestEntityPaths paths;

    @BeforeEach
    void setUp() {
        paths = new QAccountTestEntityPaths();
    }

    @Test
    void accountIdsScope_addsIdInRoleAccountIds() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false);

        BooleanExpression predicate = builder.build(minimalCriteria(), context, paths, KEYWORD);

        assertThat(predicate.toString())
                .contains("account.id in [")
                .contains("1")
                .contains("2")
                .doesNotContain("competentAuthority");
    }

    @Test
    void competentAuthorityScope_addsCompetentAuthorityPredicate() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forCompetentAuthority(
                CompetentAuthorityEnum.ENGLAND, Set.of(), false);

        BooleanExpression predicate = builder.build(minimalCriteria(), context, paths, KEYWORD);

        assertThat(predicate.toString())
                .contains("account.competentAuthority = ENGLAND")
                .doesNotContain("account.id in");
    }

    @Test
    void termPredicate_usesIlikeWithoutLowerOnValue() {
        AccountSearchFilterCriteria criteria = criteriaWithTerm("abc");
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false);

        BooleanExpression predicate = builder.build(criteria, context, paths, KEYWORD);

        String expression = predicate.toString();
        assertThat(expression)
                .containsIgnoringCase("ILIKE")
                .containsIgnoringCase("accountSearchAdditionalKeyword.value")
                .containsIgnoringCase("%abc%")
                .doesNotContainIgnoringCase("lower(");
    }

    @Test
    void termPredicate_partialCaseInsensitiveMatch() {
        AccountSearchFilterCriteria criteria = criteriaWithTerm("abc");
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false);

        BooleanExpression predicate = builder.build(criteria, context, paths, KEYWORD);

        String expression = predicate.toString();
        assertThat(expression)
                .containsIgnoringCase("accountSearchAdditionalKeyword.value")
                .containsIgnoringCase("abc")
                .doesNotContain("= ABC123");
    }

    @Test
    void termPredicate_trimsTermWithoutChangingCase() {
        AccountSearchFilterCriteria criteria = criteriaWithTerm("  Ship  ");
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false);

        BooleanExpression predicate = builder.build(criteria, context, paths, KEYWORD);

        assertThat(predicate.toString())
                .containsIgnoringCase("accountSearchAdditionalKeyword.value")
                .contains("Ship")
                .doesNotContain("%ship%");
        assertThat(AccountSearchPredicateBuilder.normalizeTerm("  Ship  ")).isEqualTo("Ship");
    }

    @Test
    void statusPredicate_delegatesToPathsStatusIn() {
        @SuppressWarnings("unchecked")
        AccountSearchEntityPaths<uk.gov.netz.api.account.domain.Account> mockPaths =
                mock(AccountSearchEntityPaths.class);
        when(mockPaths.idPath()).thenReturn(paths.idPath());
        BooleanExpression statusPredicate = paths.idPath().eq(42L);
        when(mockPaths.statusIn(any())).thenReturn(statusPredicate);

        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .paging(paging())
                .statuses(Set.of(TestAccountStatus.DUMMY))
                .build();
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false);

        BooleanExpression predicate = builder.build(criteria, context, mockPaths, KEYWORD);

        verify(mockPaths).statusIn(criteria.getStatuses());
        assertThat(predicate.toString()).contains("account.id = 42");
    }

    @Test
    void contactFilterActive_addsIdInContactAccountIds() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(10L, 20L), true);

        BooleanExpression predicate = builder.build(minimalCriteria(), context, paths, KEYWORD);

        assertThat(predicate.toString())
                .contains("account.id = 1")
                .contains("account.id in [")
                .contains("10")
                .contains("20");
    }

    @Test
    void optionalFiltersIgnored_whenAbsent() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false);

        BooleanExpression predicate = builder.build(minimalCriteria(), context, paths, KEYWORD);

        String expression = predicate.toString();
        assertThat(expression).isEqualTo("account.id = 1");
        assertThat(expression)
                .doesNotContain("accountSearchAdditionalKeyword")
                .doesNotContain("competentAuthority");
    }

    @Test
    void contactFilterInactive_ignoresContactAccountIds() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(99L), false);

        BooleanExpression predicate = builder.build(minimalCriteria(), context, paths, KEYWORD);

        assertThat(predicate.toString()).isEqualTo("account.id = 1");
    }

    @Test
    void effectivelyEmptyContext_isServiceLayerResponsibility() {
        // Repository must not run when scope/contact resolution yields no searchable accounts.
        assertThat(AccountSearchQueryContext.forAccountIds(Set.of(), Set.of(), false).isEffectivelyEmpty()).isTrue();
        assertThat(AccountSearchQueryContext.forCompetentAuthority(null, Set.of(), false).isEffectivelyEmpty()).isTrue();
        assertThat(AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), true).isEffectivelyEmpty()).isTrue();
        assertThat(AccountSearchQueryContext.forAccountIds(Set.of(1L), Set.of(), false).isEffectivelyEmpty()).isFalse();
    }

    @Test
    void combinesPredicatesWithAnd() {
        paths.setStatusInResult(paths.idPath().eq(99L));

        AccountSearchFilterCriteria criteria = AccountSearchFilterCriteria.builder()
                .paging(paging())
                .term("alpha")
                .statuses(Set.of(TestAccountStatus.DUMMY))
                .build();
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(5L), true);

        BooleanExpression predicate = builder.build(criteria, context, paths, KEYWORD);

        String expression = predicate.toString();
        assertThat(expression).contains(" && ");
        assertThat(expression).contains("account.id = 1");
        assertThat(expression)
                .containsIgnoringCase("accountSearchAdditionalKeyword.value")
                .containsIgnoringCase("alpha");
        assertThat(expression).contains("account.id = 99");
        assertThat(expression).contains("account.id = 5");
    }

    private static PagingRequest paging() {
        return PagingRequest.builder().pageNumber(0).pageSize(20).build();
    }

    private static AccountSearchFilterCriteria minimalCriteria() {
        return AccountSearchFilterCriteria.builder().paging(paging()).build();
    }

    private static AccountSearchFilterCriteria criteriaWithTerm(String term) {
        return AccountSearchFilterCriteria.builder().paging(paging()).term(term).build();
    }
}
