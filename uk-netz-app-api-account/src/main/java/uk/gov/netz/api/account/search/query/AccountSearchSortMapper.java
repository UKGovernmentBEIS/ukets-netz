package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.StringExpression;
import org.springframework.data.domain.Sort;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.search.criteria.AccountSearchCommonSortField;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchSortField;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;

/** Maps common sort fields to QueryDSL order specifiers. */
public class AccountSearchSortMapper<T extends Account> {

    public OrderSpecifier<String> toOrderSpecifier(
            AccountSearchSortField sortField,
            Sort.Direction direction,
            AccountSearchEntityPaths<T> paths) {
        if (!(sortField instanceof AccountSearchCommonSortField commonSortField)) {
            throw new IllegalArgumentException("Sort field not supported: " + sortField);
        }
        return switch (commonSortField) {
            case OPERATOR_NAME -> orderBy(requirePath(paths.namePath(), commonSortField), direction);
            case ACCOUNT_ID -> orderBy(requirePath(paths.businessIdPath(), commonSortField), direction);
            case STATUS -> orderBy(requirePath(paths.statusPath(), commonSortField), direction);
        };
    }

    public OrderSpecifier<String> fromCriteria(
            AccountSearchFilterCriteria criteria,
            AccountSearchEntityPaths<T> paths) {
        return toOrderSpecifier(criteria.getSortField(), criteria.getSortDirection(), paths);
    }

    private static OrderSpecifier<String> orderBy(
            StringExpression path,
            Sort.Direction direction) {
        return direction.isAscending() ? path.asc() : path.desc();
    }

    private static StringExpression requirePath(
            StringExpression path,
            AccountSearchCommonSortField sortField) {
        if (path == null) {
            throw new IllegalArgumentException("Sort field not supported: " + sortField);
        }
        return path;
    }
}
