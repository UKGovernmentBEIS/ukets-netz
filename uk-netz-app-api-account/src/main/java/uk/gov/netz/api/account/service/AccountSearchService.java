package uk.gov.netz.api.account.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.repository.AccountSearchRepository;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@Service
@RequiredArgsConstructor
public class AccountSearchService extends AccountBaseSearchService<Account> {

    private final AccountSearchRepository accountSearchRepository;

	public AccountSearchResults searchAccounts(List<Long> accountIds, AccountSearchCriteria accountSearchCriteria) {
		if (accountIds == null || accountIds.isEmpty()) {
			return AccountSearchResults.emptyAccountSearchResults();
		}

		final Page<Account> pageResults = accountSearchRepository.searchAccounts(
				getPageRequest(accountSearchCriteria), 
				accountIds,
				getSearchTerm(accountSearchCriteria));
		return buildAccountSearchResults(pageResults);
	}

	public AccountSearchResults searchAccounts(CompetentAuthorityEnum competentAuthority,
			AccountSearchCriteria accountSearchCriteria) {
		final Page<Account> pageResults = accountSearchRepository.searchAccounts(
				getPageRequest(accountSearchCriteria), 
				competentAuthority,
				getSearchTerm(accountSearchCriteria));
		return buildAccountSearchResults(pageResults);
	}

}