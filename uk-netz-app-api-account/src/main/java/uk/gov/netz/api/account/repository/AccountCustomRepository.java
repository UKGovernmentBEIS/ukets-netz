package uk.gov.netz.api.account.repository;

import uk.gov.netz.api.account.domain.Account;

import java.util.Optional;

public interface AccountCustomRepository {
    Optional<Account> findByIdForUpdate(Long id);
}
