package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.QAccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;

/**
 * Builds AND-combined QueryDSL predicates for generic account search.
 * Empty role scope / blocking contact filter: service must check {@link AccountSearchQueryContext#isEffectivelyEmpty()}
 * before calling the repository.
 */
public class AccountSearchPredicateBuilder<T extends Account> {

    public BooleanExpression build(
            AccountSearchFilterCriteria criteria,
            AccountSearchQueryContext context,
            AccountSearchEntityPaths<T> paths,
            QAccountSearchAdditionalKeyword keyword) {

        BooleanExpression predicate = roleScopePredicate(context, paths);

        if (criteria.hasTerm()) {
            predicate = predicate.and(keywordPredicate(criteria, keyword));
        }

        if (criteria.hasStatusFilter()) {
            BooleanExpression statusPredicate = paths.statusIn(criteria.getStatuses());
            if (statusPredicate != null) {
                predicate = predicate.and(statusPredicate);
            }
        }

        if (context.isContactFilterActive()) {
            predicate = predicate.and(contactAccountIdsPredicate(context, paths));
        }

        return predicate;
    }

    private BooleanExpression roleScopePredicate(
            AccountSearchQueryContext context,
            AccountSearchEntityPaths<T> paths) {
        return switch (context.getScope()) {
            case ACCOUNT_IDS -> paths.idPath().in(context.getRoleAccountIds());
            case COMPETENT_AUTHORITY -> paths.competentAuthorityPath().eq(context.getCompetentAuthority());
        };
    }

    private BooleanExpression keywordPredicate(
            AccountSearchFilterCriteria criteria,
            QAccountSearchAdditionalKeyword keyword) {
        String term = normalizeTerm(criteria.getTerm());
        return Expressions.booleanTemplate("{0} ILIKE {1}", keyword.value, "%" + term + "%");
    }

    private BooleanExpression contactAccountIdsPredicate(
            AccountSearchQueryContext context,
            AccountSearchEntityPaths<T> paths) {
        return paths.idPath().in(context.getContactAccountIds());
    }

    static String normalizeTerm(String term) {
        return term.trim();
    }
}
