package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.TestAccountStatus;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.dto.AccountInfoDTO;
import uk.gov.netz.api.account.domain.enumeration.AccountStatus;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.account.transform.AccountMapper;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @InjectMocks
    private AccountQueryService accountQueryService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Test
    void getAccountCa() {
        final Long accountId = 1L;
        final CompetentAuthorityEnum competentAuthority = CompetentAuthorityEnum.ENGLAND;
        Account account = Mockito.mock(Account.class);

        when(account.getCompetentAuthority()).thenReturn(competentAuthority);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        CompetentAuthorityEnum accountCa = accountQueryService.getAccountCa(accountId);

        assertThat(accountCa).isEqualTo(CompetentAuthorityEnum.ENGLAND);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountCa_not_found() {
        final Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
                accountQueryService.getAccountCa(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountVerificationBodyId() {
        final Long accountId = 1L;
        final Long verificationBodyId = 2L;
        Account account = Mockito.mock(Account.class);

        when(account.getVerificationBodyId()).thenReturn(verificationBodyId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        //assert
        Optional<Long> accountVerificationBody = accountQueryService.getAccountVerificationBodyId(accountId);

        assertThat(accountVerificationBody).isPresent();
        assertEquals(verificationBodyId, accountVerificationBody.get());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountVerificationBodyId_empty() {
        final Long accountId = 1L;
        Account account = Mockito.mock(Account.class);

        when(account.getVerificationBodyId()).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Long> accountVerificationBody = accountQueryService.getAccountVerificationBodyId(accountId);

        assertThat(accountVerificationBody).isEmpty();
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountVerificationBodyId_account_not_found() {
        final Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
                accountQueryService.getAccountVerificationBodyId(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountName() {
        final Long accountId = 1L;
        final String name = "name";
        Account account = Mockito.mock(Account.class);

        when(account.getName()).thenReturn(name);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        String result = accountQueryService.getAccountName(accountId);

        assertThat(result).isEqualTo(name);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountName_not_found() {
        final Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());
        BusinessException be = assertThrows(BusinessException.class, () ->
                accountQueryService.getAccountName(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountStatus() {
        final Long accountId = 1L;
        final TestAccountStatus status = TestAccountStatus.DUMMY;
        Account account = Mockito.mock(Account.class);

        when(account.getStatus()).thenReturn(status);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        AccountStatus result = accountQueryService.getAccountStatus(accountId);

        assertThat(result).isEqualTo(status);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountStatus_not_found() {
        final Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
                accountQueryService.getAccountStatus(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountDTOById() {
        Long accountId = 1L;
        Account account = Mockito.mock(Account.class);
        AccountInfoDTO accountInfoDTO = AccountInfoDTO.builder().id(accountId).name("name").build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountInfoDTO(account)).thenReturn(accountInfoDTO);

        AccountInfoDTO result = accountQueryService.getAccountInfoDTOById(accountId);

        assertThat(result).isEqualTo(accountInfoDTO);
        verify(accountRepository, times(1)).findById(accountId);
        verify(accountMapper, times(1)).toAccountInfoDTO(account);
    }

    @Test
    void getAccountsByAccountIds() {
        Long accountId = 1L;
        List<Long> accountIds = Arrays.asList(1L, 2L, 3L);
        Account account = TestAccount.builder()
                .id(1L)
                .status(TestAccountStatus.DUMMY)
                .name("Test Account")
                .build();

        when(accountRepository.findAllByIdIn(accountIds))
                .thenReturn(Collections.singletonList(account));

        final List<Account> accounts = accountQueryService.getAccounts(accountIds);
        verify(accountRepository, times(1)).findAllByIdIn(accountIds);
        assertThat(accounts.size()).isEqualTo(1);
        assertThat(accounts.get(0).getId()).isEqualTo(accountId);
    }
    
    @Test
    void getThirdPartyDataProviderId() {
        final Long accountId = 1L;
        final Long thirdPartyDataProviderId = 2L;
        Account account = Mockito.mock(Account.class);

        when(account.getThirdPartyDataProviderId()).thenReturn(thirdPartyDataProviderId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        //assert
        Optional<Long> accountThirdPartyDataProvider = accountQueryService.getThirdPartyDataProviderId(accountId);

        assertThat(accountThirdPartyDataProvider).isPresent();
        assertEquals(thirdPartyDataProviderId, accountThirdPartyDataProvider.get());
        verify(accountRepository, times(1)).findById(accountId);
    }
    
    @Test
    void getThirdPartyDataProviderId_empty() {
        final Long accountId = 1L;
        
        Account account = Mockito.mock(Account.class);

        when(account.getThirdPartyDataProviderId()).thenReturn(null);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Long> accountThirdPartyDataProvider = accountQueryService.getThirdPartyDataProviderId(accountId);

        assertThat(accountThirdPartyDataProvider).isEmpty();
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getThirdPartyDataProviderId_account_not_found() {
        final Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
                accountQueryService.getThirdPartyDataProviderId(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
        verify(accountRepository, times(1)).findById(accountId);
    }

}
