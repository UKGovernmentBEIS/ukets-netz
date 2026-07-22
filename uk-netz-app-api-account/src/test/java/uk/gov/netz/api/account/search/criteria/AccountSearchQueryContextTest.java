package uk.gov.netz.api.account.search.criteria;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountSearchQueryContextTest {

    private static AccountSearchFilterCriteria criteriaWithContactEmail() {
        return AccountSearchFilterCriteria.builder()
                .contactEmail("user@example.com")
                .paging(PagingRequest.builder().pageNumber(0).pageSize(20).build())
                .build();
    }

    private static AccountSearchFilterCriteria criteriaWithoutContactEmail() {
        return AccountSearchFilterCriteria.builder()
                .paging(PagingRequest.builder().pageNumber(0).pageSize(20).build())
                .build();
    }

    @Test
    void forAccountIds_emptyRoleScope_isRoleScopeEmpty() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(), Set.of(), false);

        assertThat(context.isRoleScopeEmpty()).isTrue();
        assertThat(context.isEffectivelyEmpty()).isTrue();
    }

    @Test
    void forAccountIds_withIds_notEmpty() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(Set.of(1L, 2L), Set.of(), false);

        assertThat(context.isRoleScopeEmpty()).isFalse();
        assertThat(context.isEffectivelyEmpty()).isFalse();
    }

    @Test
    void forCompetentAuthority_nullCa_isRoleScopeEmpty() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forCompetentAuthority(null, Set.of(), false);

        assertThat(context.isRoleScopeEmpty()).isTrue();
    }

    @Test
    void forCompetentAuthority_withCa_notEmpty() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forCompetentAuthority(
                CompetentAuthorityEnum.ENGLAND, Set.of(), false);

        assertThat(context.isRoleScopeEmpty()).isFalse();
    }

    @Test
    void contactFilterInactive_emptyContactIds_isNotBlocking() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(), false);

        assertThat(context.isContactFilterActive()).isFalse();
        assertThat(context.isContactFilterBlocking()).isFalse();
        assertThat(context.isEffectivelyEmpty()).isFalse();
    }

    @Test
    void contactFilterActiveWithNoIds_isContactFilterBlocking() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(), true);

        assertThat(context.isContactFilterBlocking()).isTrue();
        assertThat(context.isEffectivelyEmpty()).isTrue();
    }

    @Test
    void contactFilterActiveWithResolvedIds_isNotBlocking() {
        AccountSearchQueryContext context = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(10L), true);

        assertThat(context.isContactFilterBlocking()).isFalse();
        assertThat(context.isEffectivelyEmpty()).isFalse();
    }

    @Test
    void forAccountIds_fromCriteria_setsContactFilterActiveFromCriteria() {
        AccountSearchQueryContext withEmail = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(), criteriaWithContactEmail());
        AccountSearchQueryContext withoutEmail = AccountSearchQueryContext.forAccountIds(
                Set.of(1L), Set.of(), criteriaWithoutContactEmail());

        assertThat(withEmail.isContactFilterActive()).isTrue();
        assertThat(withoutEmail.isContactFilterActive()).isFalse();
    }

    @Test
    void forCompetentAuthority_fromCriteria_setsContactFilterActiveFromCriteria() {
        AccountSearchQueryContext withEmail = AccountSearchQueryContext.forCompetentAuthority(
                CompetentAuthorityEnum.ENGLAND, Set.of(), criteriaWithContactEmail());

        assertThat(withEmail.isContactFilterActive()).isTrue();
    }

    @Test
    void normalizesNullAccountIdSetsToEmpty() {
        AccountSearchQueryContext context = AccountSearchQueryContext.builder()
                .scope(AccountSearchScope.ACCOUNT_IDS)
                .roleAccountIds(null)
                .contactAccountIds(null)
                .contactFilterActive(false)
                .build();

        assertThat(context.getRoleAccountIds()).isEmpty();
        assertThat(context.getContactAccountIds()).isEmpty();
    }
}
