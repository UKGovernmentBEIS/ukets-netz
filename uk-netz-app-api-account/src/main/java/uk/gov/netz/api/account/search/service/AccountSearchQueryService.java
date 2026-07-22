package uk.gov.netz.api.account.search.service;

import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.search.criteria.AccountSearchContactFilter;
import uk.gov.netz.api.account.search.criteria.AccountSearchFilterCriteria;
import uk.gov.netz.api.authorization.core.domain.AppUser;

/** Resolves role scope before calling the query repository. */
public interface AccountSearchQueryService<T extends Account, SR extends AccountSearchResultInfoDTO> {

    AccountSearchResults<SR> search(
            AppUser user,
            AccountSearchFilterCriteria criteria,
            AccountSearchContactFilter contactFilter);
}
