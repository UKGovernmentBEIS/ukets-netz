package uk.gov.netz.api.account.service;

import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppUser;

public interface UserRoleTypeAccountSearchService {

    AccountSearchResults getUserAccountsBySearchCriteria(AppUser appUser, AccountSearchCriteria searchCriteria);

    String getRoleType();
}
