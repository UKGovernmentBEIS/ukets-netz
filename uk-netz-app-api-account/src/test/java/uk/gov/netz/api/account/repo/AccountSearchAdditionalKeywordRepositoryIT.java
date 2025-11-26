package uk.gov.netz.api.account.repo;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.AccountSearchAdditionalKeyword;
import uk.gov.netz.api.account.repository.AccountSearchAdditionalKeywordRepository;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
public class AccountSearchAdditionalKeywordRepositoryIT extends AbstractContainerBaseTest {

    @Autowired
    private AccountSearchAdditionalKeywordRepository cut;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void searchAccounts_by_accountIds() {
    	Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
    	AccountSearchAdditionalKeyword keyword1 = createAccountSearchAdditionalKeyword(account1.getId(), "key1", "Operator_1705160674924_NEW");
    	AccountSearchAdditionalKeyword keyword2 = createAccountSearchAdditionalKeyword(account1.getId(), "key2", "UNDERLINE_NEW");
        
        Account account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, 1L, "buss2");
        createAccountSearchAdditionalKeyword(account2.getId(), "key3", "Installation_1705160674924_NEW");
        

        flushAndClear();

		List<AccountSearchAdditionalKeyword> result = cut.findByAccountId(account1.getId());

		assertThat(result).containsExactlyInAnyOrder(keyword1, keyword2);
    }
    
    @Test
    void deleteAllByAccountId() {
    	Account account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, 1L, "buss1");
    	createAccountSearchAdditionalKeyword(account1.getId(), "key1", "Operator_1705160674924_NEW");
    	createAccountSearchAdditionalKeyword(account1.getId(), "key2", "UNDERLINE_NEW");
        
        flushAndClear();

		cut.deleteAllByAccountId(account1.getId());
		
		List<AccountSearchAdditionalKeyword> result = cut.findByAccountId(account1.getId());

		assertThat(result).isEmpty();
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