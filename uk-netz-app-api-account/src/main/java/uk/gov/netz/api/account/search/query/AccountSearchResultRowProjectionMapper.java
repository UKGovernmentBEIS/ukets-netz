package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.StringExpression;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;

/** Default projection to {@link AccountSearchResultRow} for shared account search. */
public class AccountSearchResultRowProjectionMapper<T extends Account>
        implements AccountSearchProjectionMapper<T, AccountSearchResultRow> {

    @Override
    public ConstructorExpression<AccountSearchResultRow> constructorProjection(
            AccountSearchEntityPaths<T> paths) {
        return Projections.constructor(
                AccountSearchResultRow.class,
                paths.idPath(),
                paths.namePath(),
                paths.businessIdPath(),
                requirePath(paths.statusPath()));
    }

    private static StringExpression requirePath(StringExpression path) {
        if (path == null) {
            throw new IllegalArgumentException("Status path is required for account search projection");
        }
        return path;
    }
}
