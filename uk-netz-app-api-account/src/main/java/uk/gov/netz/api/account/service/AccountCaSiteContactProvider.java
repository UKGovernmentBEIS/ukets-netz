package uk.gov.netz.api.account.service;

import java.util.Optional;

public interface AccountCaSiteContactProvider {
    Optional<String> findCASiteContactByAccount(Long accountId);
}
