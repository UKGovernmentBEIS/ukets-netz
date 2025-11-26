package uk.gov.netz.api.account.repo;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.repository.AccountSearchRepository;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
public class AccountSearchRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private AccountSearchRepository cut;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void searchAccounts_by_accountIds() {
    	Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
    	createAccountSearchAdditionalKeyword(account1.getId(), "key1", "Operator_1705160674924_NEW");
        createAccountSearchAdditionalKeyword(account1.getId(), "key2", "UNDERLINE_NEW");
        
        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, 1L, "buss2");
        createAccountSearchAdditionalKeyword(account2.getId(), "key3", "Installation_1705160674924_NEW");
        
        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.ENGLAND, 1L, "buss3");
        createAccountSearchAdditionalKeyword(account3.getId(), "key4", "Operator_1705160674924");
        createAccountSearchAdditionalKeyword(account3.getId(), "key5", "Operator_1705160674924_NEW");
        
        Account account4 = createAccount(4L, "account4", CompetentAuthorityEnum.ENGLAND, 1L, "buss4");
        createAccountSearchAdditionalKeyword(account4.getId(), "key6", "Installation_1705160674924");
        
        final String term = "New ".toLowerCase().trim();
        

        final int pageSize = 20;
        final PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by("id"));

        flushAndClear();

		final Page<Account> result = cut.searchAccounts(pageRequest,
				List.of(account1.getId(), account2.getId(), account3.getId(), account4.getId()), term);

		assertThat(result).extracting(Account::getId).containsExactly(1L, 2L, 3L);
    }
    
    @Test
    void searchAccounts_by_CA() {
    	Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
    	createAccountSearchAdditionalKeyword(account1.getId(), "key1", "Operator_1705160674924_NEW");
        createAccountSearchAdditionalKeyword(account1.getId(), "key2", "UNDERLINE_NEW");
        
        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, 1L, "buss2");
        createAccountSearchAdditionalKeyword(account2.getId(), "key3", "Installation_1705160674924_NEW");
        
        Account account3 = createAccount(3L, "account3", CompetentAuthorityEnum.WALES, 1L, "buss3");
        createAccountSearchAdditionalKeyword(account3.getId(), "key4", "Operator_1705160674924");
        createAccountSearchAdditionalKeyword(account3.getId(), "key5", "Operator_1705160674924_NEW");
        
        Account account4 = createAccount(4L, "account4", CompetentAuthorityEnum.OPRED, 1L, "buss4");
        createAccountSearchAdditionalKeyword(account4.getId(), "key6", "Installation_1705160674924");
        
        final String term = "New ".toLowerCase().trim();
        

        final int pageSize = 20;
        final PageRequest pageRequest = PageRequest.of(0, pageSize, Sort.by("id").descending());

        flushAndClear();

		final Page<Account> result = cut.searchAccounts(pageRequest, CompetentAuthorityEnum.ENGLAND, term);

		assertThat(result).extracting(Account::getId).containsExactly(2L, 1L);
    }

    private AccountSearchAdditionalKeyword createAccountSearchAdditionalKeyword(Long accountId, String key, String val) {
        AccountSearchAdditionalKeyword accountSearchAdditionalKeyword = AccountSearchAdditionalKeyword.builder()
                .accountId(accountId)
                .key(key)
                .value(val)
                .build();

        entityManager.persist(accountSearchAdditionalKeyword);
        return accountSearchAdditionalKeyword;
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
