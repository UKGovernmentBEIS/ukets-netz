package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountThirdPartyDataProviderUnappointServiceTest {

    @InjectMocks
    private AccountThirdPartyDataProviderUnappointService service;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void unappointAccountAppointedToThirdPartyDataProvider() {
        Long accountId = 1L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(2L).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        service.unappointAccountAppointedToThirdPartyDataProvider(accountId);

        assertThat(account.getThirdPartyDataProviderId()).isNull();

        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void unappointAccountAppointedToThirdPartyDataProvider_throws_resource_not_found() {
        Long accountId = 1L;
        Long thirdPartyDataProviderId = 2L;
        Account account = TestAccount.builder().id(accountId).thirdPartyDataProviderId(thirdPartyDataProviderId).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        BusinessException be = assertThrows(BusinessException.class, () ->
            service.unappointAccountAppointedToThirdPartyDataProvider(accountId));

        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        assertThat(account.getThirdPartyDataProviderId()).isEqualTo(thirdPartyDataProviderId);

        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }
}