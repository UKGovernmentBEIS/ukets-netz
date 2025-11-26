package uk.gov.netz.api.account.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppUser;

@Service
@RequiredArgsConstructor
public class AccountSearchServiceDelegator {

    private final List<UserRoleTypeAccountSearchService> uerRoleTypeAccountSearchService;

    public AccountSearchResults getAccountsByUserAndSearchCriteria(AppUser user, AccountSearchCriteria searchCriteria) {
        return getUserService(user).map(service -> service.getUserAccountsBySearchCriteria(user, searchCriteria))
                .orElseThrow(() -> new UnsupportedOperationException(
                String.format("Fetching accounts for role type %s is not supported", user.getRoleType())));
    }

    private Optional<UserRoleTypeAccountSearchService> getUserService(AppUser user) {
        return uerRoleTypeAccountSearchService.stream()
                .filter(accountSearchService -> accountSearchService.getRoleType().equals(user.getRoleType()))
                .findAny();
    }

}
