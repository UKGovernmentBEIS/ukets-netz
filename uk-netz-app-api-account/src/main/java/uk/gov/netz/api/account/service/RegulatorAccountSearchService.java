package uk.gov.netz.api.account.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;


@Service
@RequiredArgsConstructor
public class RegulatorAccountSearchService implements UserRoleTypeAccountSearchService {

	private final AccountSearchService accountSearchService;
	
    @Override
    public AccountSearchResults getUserAccountsBySearchCriteria(AppUser appUser, AccountSearchCriteria searchCriteria) {
        return accountSearchService.searchAccounts(appUser.getCompetentAuthority(), searchCriteria);
    }

    @Override
    public String getRoleType() {
        return RoleTypeConstants.REGULATOR;
    }
}
