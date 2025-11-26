package uk.gov.netz.api.account.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.services.authorization.verifier.VerifierAccountAccessService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class VerifierAccountSearchService implements UserRoleTypeAccountSearchService {
	
	private final AccountSearchService accountSearchService;
    private final VerifierAccountAccessService verifierAccountAccessService;

    @Override
    public AccountSearchResults getUserAccountsBySearchCriteria(AppUser appUser, AccountSearchCriteria searchCriteria) {
		return accountSearchService.searchAccounts(
				new ArrayList<>(verifierAccountAccessService.findAuthorizedAccountIds(appUser)), searchCriteria);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.VERIFIER;
    }
}
