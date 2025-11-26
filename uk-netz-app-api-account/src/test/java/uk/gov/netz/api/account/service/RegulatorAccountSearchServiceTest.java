package uk.gov.netz.api.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

@ExtendWith(MockitoExtension.class)
public class RegulatorAccountSearchServiceTest {

	@InjectMocks
    private RegulatorAccountSearchService cut;
    
    @Mock
    private AccountSearchService accountSearchService;

    @Test
    void getUserAccountsBySearchCriteria() {
        final AppUser appUser = AppUser.builder()
                .authorities(List.of(AppAuthority.builder()
                                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                                .accountId(1L)
                                .build(),
                        AppAuthority.builder()
                                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                                .accountId(2L)
                                .build(),
                        AppAuthority.builder()
                                .competentAuthority(CompetentAuthorityEnum.ENGLAND)
                                .accountId(3L)
                                .build())).build();

        final Account account1 = buildAccount(1L, "Account_1", "business_id_1", TestAccountStatus.DUMMY);
        final Account account2 = buildAccount(2L, "Account_2", "business_id_2", TestAccountStatus.DUMMY2);

        final AccountSearchCriteria searchCriteria = AccountSearchCriteria.builder()
                .term("DUMMY2")
                .paging(PagingRequest.builder()
                        .pageSize(5)
                        .pageNumber(0)
                        .build())
                .build();
        
        AccountSearchResults expectedResult = AccountSearchResults.builder()
        		.accounts(List.of(
        				new AccountSearchResultInfoDTO(account1.getId(), account1.getName(), account1.getBusinessId(), account1.getStatus()),
        				new AccountSearchResultInfoDTO(account2.getId(), account2.getName(), account2.getBusinessId(), account2.getStatus())
        				))
        		.total(2L)
        		.build();

        when(accountSearchService.searchAccounts(appUser.getCompetentAuthority(), searchCriteria))
                .thenReturn(expectedResult);

        // invoke
        final AccountSearchResults actualResult = cut.getUserAccountsBySearchCriteria(appUser, searchCriteria);

        // verify
        verify(accountSearchService, times(1)).searchAccounts(appUser.getCompetentAuthority(), searchCriteria);
        assertThat(actualResult).isEqualTo(expectedResult);
    }
    
    @Test
    void getRoleType() {
    	assertThat(cut.getRoleType()).isEqualTo(RoleTypeConstants.REGULATOR);
    }

    private Account buildAccount(Long id, String accountName, String businessId, TestAccountStatus status) {
        return TestAccount.builder()
                .id(id)
                .status(status)
                .emissionTradingScheme(TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME)
                .name(accountName)
                .businessId(businessId)
                .build();
    }
}
