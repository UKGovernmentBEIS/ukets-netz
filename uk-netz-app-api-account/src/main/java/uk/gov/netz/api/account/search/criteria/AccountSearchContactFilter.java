package uk.gov.netz.api.account.search.criteria;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Set;

/** Pre-resolved contact email filter for account search. */
@Getter
@EqualsAndHashCode
public class AccountSearchContactFilter {

    private final Set<Long> accountIds;
    private final boolean active;

    private AccountSearchContactFilter(Set<Long> accountIds, boolean active) {
        this.accountIds = accountIds == null ? Set.of() : Set.copyOf(accountIds);
        this.active = active;
    }

    public static AccountSearchContactFilter none() {
        return new AccountSearchContactFilter(Set.of(), false);
    }

    public static AccountSearchContactFilter of(Set<Long> accountIds) {
        return new AccountSearchContactFilter(accountIds, true);
    }

    /** True when contact email was requested but resolved to no account IDs. */
    public boolean isBlocking() {
        return active && accountIds.isEmpty();
    }
}
