package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria;
import uk.gov.netz.api.account.domain.dto.AccountSearchCriteria.SortBy;
import uk.gov.netz.api.account.domain.dto.AccountSearchResultInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountSearchResults;
import uk.gov.netz.api.account.repository.AccountSearchRepository;
import uk.gov.netz.api.common.domain.PagingRequest;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountSearchServiceTest {

    @InjectMocks
    private AccountSearchService cut;

    @Mock
    private AccountSearchRepository accountSearchRepository;

    @Test
    void searchAccounts_by_AccountIds() {
        List<Long> accountIds = Arrays.asList(1L, 2L, 3L);
        List<Account> matchedAccounts = List.of(
        		TestAccount.builder().id(1L).name("name1").businessId("bus1").status(TestAccountStatus.DUMMY).build(),
        		TestAccount.builder().id(2L).name("name2").businessId("bus2").status(TestAccountStatus.DUMMY2).build()
        		);
        String termOriginal = "NEW ";
        String termFinal = termOriginal.toLowerCase().trim();
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageSize(5)
                .pageNumber(0)
                .build();
        AccountSearchCriteria accountSearchCriteria = AccountSearchCriteria.builder()
                .term(termOriginal)
                .paging(pagingRequest)
                .sortBy(SortBy.ACCOUNT_BUSINESS_ID)
                .direction(Direction.DESC)
                .build();

        PageRequest pageRequest = PageRequest.of(
                accountSearchCriteria.getPaging().getPageNumber(),
                accountSearchCriteria.getPaging().getPageSize(),
                Sort.by("businessId").descending()
                );

        final PageImpl<Account> page = new PageImpl<>(matchedAccounts);

        when(accountSearchRepository.searchAccounts(pageRequest, accountIds, termFinal)).thenReturn(page);

        //invoke
        final AccountSearchResults results = cut.searchAccounts(accountIds, accountSearchCriteria);

        //verify
        assertThat(results.getTotal()).isEqualTo(2);
        assertThat(results.getAccounts()).containsExactly(
        		new AccountSearchResultInfoDTO(1L, "name1", "bus1", TestAccountStatus.DUMMY),
        		new AccountSearchResultInfoDTO(2L, "name2", "bus2", TestAccountStatus.DUMMY2)
        		);
        
        verify(accountSearchRepository, times(1)).searchAccounts(pageRequest, accountIds, termFinal);
    }
    
    @Test
    void searchAccounts_by_AccountIds_no_sort() {
        List<Long> accountIds = Arrays.asList(1L, 2L, 3L);
        List<Account> matchedAccounts = List.of(
        		TestAccount.builder().id(1L).name("name1").businessId("bus1").status(TestAccountStatus.DUMMY).build(),
        		TestAccount.builder().id(2L).name("name2").businessId("bus2").status(TestAccountStatus.DUMMY2).build()
        		);
        String termOriginal = "NEW ";
        String termFinal = termOriginal.toLowerCase().trim();
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageSize(5)
                .pageNumber(0)
                .build();
        AccountSearchCriteria accountSearchCriteria = AccountSearchCriteria.builder()
                .term(termOriginal)
                .paging(pagingRequest)
                .build();

        PageRequest pageRequest = PageRequest.of(
                accountSearchCriteria.getPaging().getPageNumber(),
                accountSearchCriteria.getPaging().getPageSize()
                );

        final PageImpl<Account> page = new PageImpl<>(matchedAccounts);

        when(accountSearchRepository.searchAccounts(pageRequest, accountIds, termFinal)).thenReturn(page);

        //invoke
        final AccountSearchResults results = cut.searchAccounts(accountIds, accountSearchCriteria);

        //verify
        assertThat(results.getTotal()).isEqualTo(2);
        assertThat(results.getAccounts()).containsExactly(
        		new AccountSearchResultInfoDTO(1L, "name1", "bus1", TestAccountStatus.DUMMY),
        		new AccountSearchResultInfoDTO(2L, "name2", "bus2", TestAccountStatus.DUMMY2)
        		);
        
        verify(accountSearchRepository, times(1)).searchAccounts(pageRequest, accountIds, termFinal);
    }
    
    @Test
    void searchAccounts_by_AccountIds_no_direction() {
        List<Long> accountIds = Arrays.asList(1L, 2L, 3L);
        List<Account> matchedAccounts = List.of(
        		TestAccount.builder().id(1L).name("name1").businessId("bus1").status(TestAccountStatus.DUMMY).build(),
        		TestAccount.builder().id(2L).name("name2").businessId("bus2").status(TestAccountStatus.DUMMY2).build()
        		);
        String termOriginal = "NEW ";
        String termFinal = termOriginal.toLowerCase().trim();
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageSize(5)
                .pageNumber(0)
                .build();
        AccountSearchCriteria accountSearchCriteria = AccountSearchCriteria.builder()
                .term(termOriginal)
                .paging(pagingRequest)
                .sortBy(SortBy.ACCOUNT_BUSINESS_ID)
                .build();

        PageRequest pageRequest = PageRequest.of(
                accountSearchCriteria.getPaging().getPageNumber(),
                accountSearchCriteria.getPaging().getPageSize(),
                Sort.by("businessId").ascending()
                );

        final PageImpl<Account> page = new PageImpl<>(matchedAccounts);

        when(accountSearchRepository.searchAccounts(pageRequest, accountIds, termFinal)).thenReturn(page);

        //invoke
        final AccountSearchResults results = cut.searchAccounts(accountIds, accountSearchCriteria);

        //verify
        assertThat(results.getTotal()).isEqualTo(2);
        assertThat(results.getAccounts()).containsExactly(
        		new AccountSearchResultInfoDTO(1L, "name1", "bus1", TestAccountStatus.DUMMY),
        		new AccountSearchResultInfoDTO(2L, "name2", "bus2", TestAccountStatus.DUMMY2)
        		);
        
        verify(accountSearchRepository, times(1)).searchAccounts(pageRequest, accountIds, termFinal);
    }
    
    @Test
    void searchAccounts_by_CA() {
    	CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.SCOTLAND;
        List<Account> matchedAccounts = List.of(
        		TestAccount.builder().id(1L).name("name1").businessId("bus1").status(TestAccountStatus.DUMMY).build(),
        		TestAccount.builder().id(2L).name("name2").businessId("bus2").status(TestAccountStatus.DUMMY2).build()
        		);
        String termOriginal = "NEW ";
        String termFinal = termOriginal.toLowerCase().trim();
        PagingRequest pagingRequest = PagingRequest.builder()
                .pageSize(5)
                .pageNumber(0)
                .build();
        AccountSearchCriteria accountSearchCriteria = AccountSearchCriteria.builder()
                .term(termOriginal)
                .paging(pagingRequest)
                .sortBy(SortBy.ACCOUNT_ID)
                .direction(Direction.DESC)
                .build();

        PageRequest pageRequest = PageRequest.of(
                accountSearchCriteria.getPaging().getPageNumber(),
                accountSearchCriteria.getPaging().getPageSize(),
                Sort.by("id").descending()
                );

        final PageImpl<Account> page = new PageImpl<>(matchedAccounts);

        when(accountSearchRepository.searchAccounts(pageRequest, competentAuthority, termFinal)).thenReturn(page);

        //invoke
        final AccountSearchResults results = cut.searchAccounts(competentAuthority, accountSearchCriteria);

        //verify
        assertThat(results.getTotal()).isEqualTo(2);
        assertThat(results.getAccounts()).containsExactly(
        		new AccountSearchResultInfoDTO(1L, "name1", "bus1", TestAccountStatus.DUMMY),
        		new AccountSearchResultInfoDTO(2L, "name2", "bus2", TestAccountStatus.DUMMY2)
        		);
        
        verify(accountSearchRepository, times(1)).searchAccounts(pageRequest, competentAuthority, termFinal);
    }

}
