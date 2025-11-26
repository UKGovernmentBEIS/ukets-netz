package uk.gov.netz.api.account.service;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.PagingRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class AccountSearchServiceDelegatorTest {

    private AccountSearchServiceDelegator accountSearchServiceDelegator;

    private RegulatorAccountSearchService regulatorAccountSearchAdditionalKeywordService;

    private OperatorAccountSearchService operatorAccountSearchAdditionalKeywordService;

    private VerifierAccountSearchService verifierAccountSearchAdditionalKeywordService;

    private final AppUser OPERATOR = AppUser.builder().roleType(RoleTypeConstants.OPERATOR).build();
    private final AppUser REGULATOR = AppUser.builder().roleType(RoleTypeConstants.REGULATOR).build();
    private final AppUser VERIFIER = AppUser.builder().roleType(RoleTypeConstants.VERIFIER).build();

    @BeforeAll
    void setup() {
        operatorAccountSearchAdditionalKeywordService = Mockito.mock(OperatorAccountSearchService.class);
        regulatorAccountSearchAdditionalKeywordService = Mockito.mock(RegulatorAccountSearchService.class);
        verifierAccountSearchAdditionalKeywordService = Mockito.mock(VerifierAccountSearchService.class);
        accountSearchServiceDelegator = new AccountSearchServiceDelegator(
                List.of(operatorAccountSearchAdditionalKeywordService, regulatorAccountSearchAdditionalKeywordService, verifierAccountSearchAdditionalKeywordService));
        when(operatorAccountSearchAdditionalKeywordService.getRoleType()).thenReturn(RoleTypeConstants.OPERATOR);
        when(regulatorAccountSearchAdditionalKeywordService.getRoleType()).thenReturn(RoleTypeConstants.REGULATOR);
        when(verifierAccountSearchAdditionalKeywordService.getRoleType()).thenReturn(RoleTypeConstants.VERIFIER);
    }

    @Test
    void getAccountsByUserAndSearchCriteria_operator() {
        final AccountSearchCriteria searchCriteria = getSearchCriteria();
        final AccountSearchResults accountSearchResults = getAccountSearchResults();

        when(operatorAccountSearchAdditionalKeywordService.getUserAccountsBySearchCriteria(OPERATOR, searchCriteria)).thenReturn(accountSearchResults);

        // invoke
        accountSearchServiceDelegator.getAccountsByUserAndSearchCriteria(OPERATOR, searchCriteria);

        //verify
        verify(operatorAccountSearchAdditionalKeywordService, times(1)).getUserAccountsBySearchCriteria(OPERATOR, searchCriteria);
    }

    @Test
    void getAccountsByUserAndSearchCriteria_regulator() {
        final AccountSearchCriteria searchCriteria = getSearchCriteria();
        final AccountSearchResults accountSearchResults = getAccountSearchResults();

        when(regulatorAccountSearchAdditionalKeywordService.getUserAccountsBySearchCriteria(REGULATOR, searchCriteria)).thenReturn(accountSearchResults);

        // invoke
        accountSearchServiceDelegator.getAccountsByUserAndSearchCriteria(REGULATOR, searchCriteria);

        // verify
        verify(regulatorAccountSearchAdditionalKeywordService, times(1)).getUserAccountsBySearchCriteria(REGULATOR, searchCriteria);
    }

    @Test
    void getAccountsByUserAndSearchCriteria_verifier() {
        final AccountSearchCriteria searchCriteria = getSearchCriteria();
        final AccountSearchResults accountSearchResults = getAccountSearchResults();

        when(verifierAccountSearchAdditionalKeywordService.getUserAccountsBySearchCriteria(VERIFIER, searchCriteria)).thenReturn(accountSearchResults);

        // invoke
        accountSearchServiceDelegator.getAccountsByUserAndSearchCriteria(VERIFIER, searchCriteria);

        // verify
        verify(verifierAccountSearchAdditionalKeywordService, times(1)).getUserAccountsBySearchCriteria(VERIFIER, searchCriteria);
    }

    @Test
    void getAccountsByUserAndSearchCriteria_Error() {
        final AccountSearchCriteria searchCriteria = getSearchCriteria();

        UnsupportedOperationException be = assertThrows(UnsupportedOperationException.class, () -> {
            accountSearchServiceDelegator.getAccountsByUserAndSearchCriteria(AppUser.builder().roleType("ANOTHER_ROLE").build(), searchCriteria);
        });
        assertThat(be.getMessage()).isEqualTo("Fetching accounts for role type ANOTHER_ROLE is not supported");
    }

    private AccountSearchCriteria getSearchCriteria() {
        String term = "NEW";
        final PagingRequest pageRequest = PagingRequest.builder()
                .pageSize(5)
                .pageNumber(0)
                .build();
        return AccountSearchCriteria.builder()
                .term(term)
                .paging(pageRequest)
                .build();
    }

    private AccountSearchResults getAccountSearchResults() {
        final AccountSearchResultInfoDTO accountSearchResultInfoDTO1 =
                new AccountSearchResultInfoDTO(1L, "Account_1", "business_id_1", TestAccountStatus.DUMMY2);

        final AccountSearchResultInfoDTO accountSearchResultInfoDTO2 =
                new AccountSearchResultInfoDTO(2L, "Account_2", "business_id_2", TestAccountStatus.DUMMY2);

        return AccountSearchResults.builder()
                .accounts(List.of(accountSearchResultInfoDTO1, accountSearchResultInfoDTO2))
                .total(2L)
                .build();
    }
}
