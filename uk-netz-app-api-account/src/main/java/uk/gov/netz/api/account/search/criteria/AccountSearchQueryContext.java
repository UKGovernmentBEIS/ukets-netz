package uk.gov.netz.api.account.search.criteria;

import lombok.Builder;
import lombok.Getter;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Set;

/** Role scope and resolved contact filter for an account search. */
@Getter
public class AccountSearchQueryContext {

    private final AccountSearchScope scope;
    private final Set<Long> roleAccountIds;
    private final CompetentAuthorityEnum competentAuthority;
    private final Set<Long> contactAccountIds;
    /** True when the request included contact email (IDs may still be empty). */
    private final boolean contactFilterActive;

    @Builder
    private AccountSearchQueryContext(
            AccountSearchScope scope,
            Set<Long> roleAccountIds,
            CompetentAuthorityEnum competentAuthority,
            Set<Long> contactAccountIds,
            boolean contactFilterActive) {
        this.scope = scope;
        this.roleAccountIds = roleAccountIds == null ? Set.of() : Set.copyOf(roleAccountIds);
        this.competentAuthority = competentAuthority;
        this.contactAccountIds = contactAccountIds == null ? Set.of() : Set.copyOf(contactAccountIds);
        this.contactFilterActive = contactFilterActive;
    }

    public static AccountSearchQueryContext forAccountIds(
            Set<Long> accountIds,
            Set<Long> contactAccountIds,
            AccountSearchFilterCriteria criteria) {
        return forAccountIds(accountIds, contactAccountIds, criteria.hasContactEmail());
    }

    public static AccountSearchQueryContext forAccountIds(
            Set<Long> accountIds,
            Set<Long> contactAccountIds,
            boolean contactFilterActive) {
        return AccountSearchQueryContext.builder()
                .scope(AccountSearchScope.ACCOUNT_IDS)
                .roleAccountIds(accountIds)
                .contactAccountIds(contactAccountIds)
                .contactFilterActive(contactFilterActive)
                .build();
    }

    public static AccountSearchQueryContext forCompetentAuthority(
            CompetentAuthorityEnum competentAuthority,
            Set<Long> contactAccountIds,
            AccountSearchFilterCriteria criteria) {
        return forCompetentAuthority(competentAuthority, contactAccountIds, criteria.hasContactEmail());
    }

    public static AccountSearchQueryContext forCompetentAuthority(
            CompetentAuthorityEnum competentAuthority,
            Set<Long> contactAccountIds,
            boolean contactFilterActive) {
        return AccountSearchQueryContext.builder()
                .scope(AccountSearchScope.COMPETENT_AUTHORITY)
                .competentAuthority(competentAuthority)
                .contactAccountIds(contactAccountIds)
                .contactFilterActive(contactFilterActive)
                .build();
    }

    /** True when the role scope cannot return any accounts. */
    public boolean isRoleScopeEmpty() {
        return switch (scope) {
            case ACCOUNT_IDS -> roleAccountIds.isEmpty();
            case COMPETENT_AUTHORITY -> competentAuthority == null;
        };
    }

    /** True when contact email was requested but resolved to no account IDs. */
    public boolean isContactFilterBlocking() {
        return contactFilterActive && contactAccountIds.isEmpty();
    }

    /** True when the search can short-circuit to empty results. */
    public boolean isEffectivelyEmpty() {
        return isRoleScopeEmpty() || isContactFilterBlocking();
    }
}
