package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.thirdpartydataprovider.service.ThirdPartyDataProviderQueryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountThirdPartyDataProviderAppointServiceTest {

    @InjectMocks
    private AccountThirdPartyDataProviderAppointService service;

    @Mock
    private AccountQueryService accountQueryService;
    @Mock
    private ApprovedAccountQueryService approvedAccountQueryService;
    @Mock
    private ThirdPartyDataProviderQueryService thirdPartyDataProviderQueryService;

    @Test
    void appointThirdPartyDataProviderToAccount() {
        Long thirdPartyDataProviderId = 1L;
        Long accountId = 2L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(3L).build();

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(thirdPartyDataProviderQueryService.existsById(thirdPartyDataProviderId)).thenReturn(true);

        service.appointThirdPartyDataProviderToAccount(thirdPartyDataProviderId, accountId);

        assertThat(account.getThirdPartyDataProviderId()).isEqualTo(thirdPartyDataProviderId);

        verify(accountQueryService).getAccountById(accountId);
        verify(approvedAccountQueryService).isAccountApproved(account);
        verify(thirdPartyDataProviderQueryService).existsById(thirdPartyDataProviderId);

        verifyNoMoreInteractions(accountQueryService, approvedAccountQueryService, thirdPartyDataProviderQueryService);
    }

    @Test
    void appointthirdpartydataprovidertoaccount_throws_third_party_data_provider_already_appointed_to_account() {
        Long thirdPartyDataProviderId = 1L;
        Long accountId = 2L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(thirdPartyDataProviderId).build();

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(thirdPartyDataProviderQueryService.existsById(thirdPartyDataProviderId)).thenReturn(true);

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.appointThirdPartyDataProviderToAccount(thirdPartyDataProviderId, accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.THIRD_PARTY_DATA_PROVIDER_ALREADY_APPOINTED_TO_ACCOUNT);

        verify(accountQueryService).getAccountById(accountId);
        verify(approvedAccountQueryService).isAccountApproved(account);
        verify(thirdPartyDataProviderQueryService).existsById(thirdPartyDataProviderId);

        verifyNoMoreInteractions(accountQueryService, approvedAccountQueryService, thirdPartyDataProviderQueryService);
    }

    @Test
    void appointthirdpartydataprovidertoaccount_throws_resource_not_found() {
        Long thirdPartyDataProviderId = 1L;
        Long accountId = 2L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(3L).build();

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(thirdPartyDataProviderQueryService.existsById(thirdPartyDataProviderId)).thenReturn(false);

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.appointThirdPartyDataProviderToAccount(thirdPartyDataProviderId, accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(accountQueryService).getAccountById(accountId);
        verify(approvedAccountQueryService).isAccountApproved(account);
        verify(thirdPartyDataProviderQueryService).existsById(thirdPartyDataProviderId);

        verifyNoMoreInteractions(accountQueryService, approvedAccountQueryService, thirdPartyDataProviderQueryService);
    }

    @Test
    void appointthirdpartydataprovidertoaccount_throws_account_invalid_status() {
        Long thirdPartyDataProviderId = 1L;
        Long accountId = 2L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(3L).build();

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(false);

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.appointThirdPartyDataProviderToAccount(thirdPartyDataProviderId, accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_INVALID_STATUS);

        verify(accountQueryService).getAccountById(accountId);
        verify(approvedAccountQueryService).isAccountApproved(account);

        verifyNoMoreInteractions(accountQueryService, approvedAccountQueryService);
        verifyNoInteractions(thirdPartyDataProviderQueryService);
    }
}