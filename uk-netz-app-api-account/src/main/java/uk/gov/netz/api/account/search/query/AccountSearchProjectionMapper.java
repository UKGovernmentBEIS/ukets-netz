package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.ConstructorExpression;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;

/** Builds QueryDSL constructor projections from scheme paths. */
public interface AccountSearchProjectionMapper<
        T extends Account,
        R extends AccountSearchResultRow> {

    ConstructorExpression<R> constructorProjection(AccountSearchEntityPaths<T> paths);
}
