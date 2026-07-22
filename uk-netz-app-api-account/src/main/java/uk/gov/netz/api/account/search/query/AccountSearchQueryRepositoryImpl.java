package uk.gov.netz.api.account.search.query;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import jakarta.persistence.EntityManager;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.QAccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.List;

/** QueryDSL implementation of generic account search. */
public class AccountSearchQueryRepositoryImpl<
        T extends Account,
        R extends AccountSearchResultRow,
        SR extends AccountSearchResultInfoDTO>
        implements AccountSearchQueryRepository<T, SR> {

    private static final QAccountSearchAdditionalKeyword KEYWORD =
            QAccountSearchAdditionalKeyword.accountSearchAdditionalKeyword;

    private final EntityManager entityManager;
    private final AccountSearchPredicateBuilder<T> predicateBuilder;
    private final AccountSearchSortMapper<T> sortMapper;
    private final AccountSearchProjectionMapper<T, R> projectionMapper;
    private final AccountSearchQueryResultsMapper<R, SR> resultsMapper;

    public static <T extends Account>
            AccountSearchQueryRepositoryImpl<T, AccountSearchResultRow, AccountSearchResultInfoDTO>
            forSharedResults(EntityManager entityManager) {
        return new AccountSearchQueryRepositoryImpl<>(
                entityManager,
                new AccountSearchPredicateBuilder<>(),
                new AccountSearchSortMapper<>(),
                new AccountSearchResultRowProjectionMapper<>(),
                new AccountSearchResultRowMapper());
    }

    public AccountSearchQueryRepositoryImpl(
            EntityManager entityManager,
            AccountSearchSortMapper<T> sortMapper,
            AccountSearchProjectionMapper<T, R> projectionMapper,
            AccountSearchQueryResultsMapper<R, SR> resultsMapper) {
        this(
                entityManager,
                new AccountSearchPredicateBuilder<>(),
                sortMapper,
                projectionMapper,
                resultsMapper);
    }

    public AccountSearchQueryRepositoryImpl(
            EntityManager entityManager,
            AccountSearchPredicateBuilder<T> predicateBuilder,
            AccountSearchSortMapper<T> sortMapper,
            AccountSearchProjectionMapper<T, R> projectionMapper,
            AccountSearchQueryResultsMapper<R, SR> resultsMapper) {
        this.entityManager = entityManager;
        this.predicateBuilder = predicateBuilder;
        this.sortMapper = sortMapper;
        this.projectionMapper = projectionMapper;
        this.resultsMapper = resultsMapper;
    }

    @Override
    public AccountSearchResults<SR> search(
            AccountSearchFilterCriteria criteria,
            AccountSearchQueryContext context,
            AccountSearchEntityPaths<T> paths) {

        JPAQuery<?> baseQuery = new JPAQuery<>(entityManager).from(paths.root());

        if (criteria.hasTerm()) {
            baseQuery.innerJoin(KEYWORD).on(KEYWORD.accountId.eq(paths.idPath()));
        }

        BooleanExpression predicate = predicateBuilder.build(criteria, context, paths, KEYWORD);
        baseQuery.where(predicate);

        long total = countDistinct(baseQuery, paths);

        PagingRequest paging = criteria.getPaging();
        List<R> rows = baseQuery
                .select(projectionMapper.constructorProjection(paths))
                .distinct()
                .orderBy(sortMapper.fromCriteria(criteria, paths))
                .offset((long) paging.getPageNumber() * paging.getPageSize())
                .limit(paging.getPageSize())
                .fetch();

        return resultsMapper.toResults(rows, total);
    }

    private long countDistinct(JPAQuery<?> baseQuery, AccountSearchEntityPaths<T> paths) {
        Long count = baseQuery.clone().select(paths.idPath().countDistinct()).fetchOne();
        return count == null ? 0L : count;
    }
}
