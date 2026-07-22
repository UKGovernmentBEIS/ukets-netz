package uk.gov.netz.api.account.search.query;

import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.account.search.criteria.AccountSearchQueryContext;
import uk.gov.netz.api.account.search.paths.AccountSearchEntityPaths;

/** Generic QueryDSL account search repository. */
public interface AccountSearchQueryRepository<T extends Account, SR extends AccountSearchResultInfoDTO> {

    AccountSearchResults<SR> search(
            AccountSearchFilterCriteria criteria,
            AccountSearchQueryContext context,
            AccountSearchEntityPaths<T> paths);
}
