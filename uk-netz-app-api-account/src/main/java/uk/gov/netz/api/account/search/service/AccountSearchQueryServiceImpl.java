package uk.gov.netz.api.account.search.service;

import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.criteria.AccountSearchContactFilter;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;
import uk.gov.netz.api.account.search.query.AccountSearchQueryRepository;
import uk.gov.netz.api.account.search.query.AccountSearchQueryResultsMapper;
import uk.gov.netz.api.account.search.query.AccountSearchResultRow;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.verifier.VerifierAccountAccessService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.Set;

/** Role routing for generic account search. */
public class AccountSearchQueryServiceImpl<
        T extends Account,
        R extends AccountSearchResultRow,
        SR extends AccountSearchResultInfoDTO>
        implements AccountSearchQueryService<T, SR> {

    private final AccountSearchQueryRepository<T, SR> repository;
    private final AccountSearchEntityPaths<T> paths;
    private final VerifierAccountAccessService verifierAccountAccessService;
    private final AccountSearchQueryResultsMapper<R, SR> emptyResultsSource;

    public AccountSearchQueryServiceImpl(
            AccountSearchQueryRepository<T, SR> repository,
            AccountSearchEntityPaths<T> paths,
            VerifierAccountAccessService verifierAccountAccessService,
            AccountSearchQueryResultsMapper<R, SR> emptyResultsSource) {
        this.repository = repository;
        this.paths = paths;
        this.verifierAccountAccessService = verifierAccountAccessService;
        this.emptyResultsSource = emptyResultsSource;
    }

    @Override
    public AccountSearchResults<SR> search(
            AppUser user,
            AccountSearchFilterCriteria criteria,
            AccountSearchContactFilter contactFilter) {
        AccountSearchQueryContext context = buildQueryContext(user, contactFilter);
        if (context.isEffectivelyEmpty()) {
            return emptyResultsSource.emptyResults();
        }
        return repository.search(criteria, context, paths);
    }

    private AccountSearchQueryContext buildQueryContext(AppUser user, AccountSearchContactFilter contactFilter) {
        Set<Long> contactAccountIds = contactFilter.getAccountIds();
        boolean contactFilterActive = contactFilter.isActive();

        return switch (user.getRoleType()) {
            case RoleTypeConstants.OPERATOR -> AccountSearchQueryContext.forAccountIds(
                    user.getAccounts(), contactAccountIds, contactFilterActive);
            case RoleTypeConstants.REGULATOR -> AccountSearchQueryContext.forCompetentAuthority(
                    user.getCompetentAuthority(), contactAccountIds, contactFilterActive);
            case RoleTypeConstants.VERIFIER -> AccountSearchQueryContext.forAccountIds(
                    verifierAccountAccessService.findAuthorizedAccountIds(user),
                    contactAccountIds,
                    contactFilterActive);
            default -> throw new UnsupportedOperationException(
                    String.format("Fetching accounts for role type %s is not supported", user.getRoleType()));
        };
    }
}
