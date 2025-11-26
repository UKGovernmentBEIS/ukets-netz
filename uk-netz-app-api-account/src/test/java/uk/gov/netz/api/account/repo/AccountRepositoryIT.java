package uk.gov.netz.api.account.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountContactInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountContactVbInfoDTO;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class AccountRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private AccountRepository repo;

    @Autowired
    private EntityManager entityManager;

    @Test
    void findAccountContactsByAccountIdsAndContactType() {
        final Long vbId = 404L;
        Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, vbId, "buss1");
        account1.getContacts().put(AccountContactType.CA_SITE, "test1");
        account1.getContacts().put(AccountContactType.PRIMARY, "primary1");
        repo.save(account1);

        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, vbId, "buss2");
        account2.getContacts().put(AccountContactType.PRIMARY, "primary2");
        repo.save(account2);

        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.ENGLAND, vbId, "buss3");
        repo.save(account3);

        List<Long> accountIds = List.of(account1.getId(), account2.getId(), account3.getId());

        flushAndClear();

        //invoke
        List<AccountContactInfoDTO> result = repo.findAccountContactsByAccountIdsAndContactType(accountIds,
                AccountContactType.CA_SITE);

        //verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAccountId()).isEqualTo(account1.getId());
    }

    @Test
    void findAccountContactsByVbAndContactType() {
        final Long vbId = 404L;
        Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, vbId, "buss1");
        account1.getContacts().put(AccountContactType.VB_SITE, "test1");
        repo.save(account1);

        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, vbId, "buss2");
        account1.getContacts().put(AccountContactType.VB_SITE, "test2");
        repo.save(account2);

        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.ENGLAND, vbId, "buss3");
        account1.getContacts().put(AccountContactType.VB_SITE, "test3");
        repo.save(account3);

        Page<AccountContactVbInfoDTO> page = repo.findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE);
        assertThat(page).hasSize(1);
    }

    @Test
    void findAccountsByContactTypeAndUserId() {
        final Long vbId = 404L;
        Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, vbId, "buss1");
        account1.getContacts().put(AccountContactType.CA_SITE, "test1");
        account1.getContacts().put(AccountContactType.PRIMARY, "primary1");
        repo.save(account1);

        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, vbId, "buss2");
        account2.getContacts().put(AccountContactType.PRIMARY, "test1");
        repo.save(account2);

        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.ENGLAND, vbId, "buss3");
        repo.save(account3);

        flushAndClear();

        List<Account> result = repo.findAccountsByContactTypeAndUserId(AccountContactType.CA_SITE, "test1");

        //verify
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(account1.getId());
    }

    @Test
    void findAllByVerificationBodyIn() {
        Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
        createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, 2L, "buss2");
        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.ENGLAND, 3L, "buss3");
        Account account4 = createAccount(4L, "account4", CompetentAuthorityEnum.ENGLAND, 3L, "buss4");

        flushAndClear();

        Set<Account> result = repo.findAllByVerificationWithContactsBodyIn(Set.of(1L, 3L));

        assertThat(result).extracting(Account::getId)
                .containsExactlyInAnyOrder(account1.getId(), account3.getId(), account4.getId());
    }

    @Test
    void findCAByIdIn() {

        createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
        createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, 2L, "buss2");
        createAccount(3L, "account3", CompetentAuthorityEnum.SCOTLAND, 3L, "buss3");
        createAccount(4L, "account4", CompetentAuthorityEnum.SCOTLAND, 3L, "buss4");

        final Set<CompetentAuthorityEnum> actual = repo.findCAByIdIn(Set.of(1L, 2L, 3L, 4L));
        assertThat(actual)
                .containsExactlyInAnyOrder(CompetentAuthorityEnum.ENGLAND, CompetentAuthorityEnum.SCOTLAND);
    }

    private Account createAccount(Long id, String accountName, CompetentAuthorityEnum ca, Long vbId, String businessId) {
        Account account = buildAccount(id, accountName, ca, vbId, businessId);
        account.getContacts().put(AccountContactType.PRIMARY, "busines1");
        account.getContacts().put(AccountContactType.SECONDARY, "busines2");

        entityManager.persist(account);
        return account;
    }

    private Account buildAccount(Long id, String accountName, CompetentAuthorityEnum ca, Long verificationBodyId, String businessId) {
        return TestAccount.builder()
                .id(id)
                .competentAuthority(ca)
                .verificationBodyId(verificationBodyId)
                .status(TestAccountStatus.DUMMY)
                .emissionTradingScheme(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME)
                .name(accountName)
                .businessId(businessId)
                .build();
    }

    protected void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
