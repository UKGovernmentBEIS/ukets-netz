package uk.gov.netz.api.account;

import org.springframework.stereotype.Repository;

import uk.gov.netz.api.account.repository.AccountBaseRepository;

@Repository
public interface TestAccountRepository extends AccountBaseRepository<TestAccount> {

}
