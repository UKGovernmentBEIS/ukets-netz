package uk.gov.netz.api.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.common.AbstractContainerBaseTest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Testcontainers
@DataJpaTest
@Import(ObjectMapper.class)
class TestAccountRepositoryIT extends AbstractContainerBaseTest {
	
	@Autowired
    private TestAccountRepository testAccountRepository;
	
	@Autowired
    private EntityManager entityManager;
	
	@Test
    void findAllByVerificationBodyAndEmissionTradingSchemeWithContactsIn() {
        Long vbId = 1L;
        Long anotherVbId = 2L;
        TestAccount account1 = createAccount(1L, "account1", CompetentAuthorityEnum.ENGLAND, vbId, "business1");
        TestAccount account2 = createAccount(2L, "account2", CompetentAuthorityEnum.ENGLAND, vbId, "business2");
        createAccount(4L, "account4", CompetentAuthorityEnum.ENGLAND, anotherVbId, "business3");

        Set<TestAccount> result = testAccountRepository.findAllByVerificationBodyAndEmissionTradingScheme(vbId,
            Set.of(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.name(), TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2.name()));

        assertThat(result).extracting(TestAccount::getId)
            .containsExactlyInAnyOrder(account1.getId(), account2.getId());
    }

	private TestAccount createAccount(Long id, String accountName, CompetentAuthorityEnum ca, Long vbId, String businessId) {
		TestAccount account = buildAccount(id, accountName, ca, vbId, businessId);
        account.getContacts().put(AccountContactType.PRIMARY, "busines1");
        account.getContacts().put(AccountContactType.SECONDARY, "busines2");
        
        entityManager.persist(account);
        return account;
    }
    
    private TestAccount buildAccount(Long id, String accountName, CompetentAuthorityEnum ca, Long verificationBodyId, String businessId) {
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
