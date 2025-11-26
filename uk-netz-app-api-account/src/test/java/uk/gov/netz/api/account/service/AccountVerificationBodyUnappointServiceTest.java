package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import uk.gov.netz.api.account.TestAccount;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.event.AccountsVerificationBodyUnappointedEvent;
import uk.gov.netz.api.account.repository.AccountBaseRepository;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountVerificationBodyUnappointServiceTest {

    @InjectMocks
    private AccountVerificationBodyUnappointService service;
    
    @Mock
    private AccountRepository accountRepository;
    
    @Mock
    private AccountBaseRepository<TestAccount> accountBaseRepository;
    
    @Mock
    private AccountVbSiteContactService accountVbSiteContactService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    @Test
    void unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes() {
        Long accountId = 1L;
        Long verificationBodyId = 1L; 
		Set<String> notAvailableAccreditationEmissionTradingSchemes = Set.of(
				TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME.getName(),
				TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME_2.getName());
        TestAccount account = mock(TestAccount.class);
        Set<TestAccount> accountsToBeUnappointed = Set.of(account);

        when(account.getId()).thenReturn(accountId);
        when(accountBaseRepository.findAllByVerificationBodyAndEmissionTradingScheme(verificationBodyId, notAvailableAccreditationEmissionTradingSchemes))
            .thenReturn(accountsToBeUnappointed);
        
        //invoke
        service.unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes(verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);
        
        verify(accountBaseRepository, times(1)).findAllByVerificationBodyAndEmissionTradingScheme(verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);
        verify(accountVbSiteContactService, times(1)).removeVbSiteContactFromAccounts(accountsToBeUnappointed);
        verify(accountVerificationBodyNotificationService, times(1)).notifyUsersForVerificationBodyUnappointment(accountsToBeUnappointed);
        verify(eventPublisher, times(1))
            .publishEvent(AccountsVerificationBodyUnappointedEvent.builder()
                .accountIds(Set.of(accountId))
                .build()
            );
        verify(account, times(1)).setVerificationBodyId(null);
    }
    
    @Test
    void unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes_empty_ref_num_list() {
        Long verificationBodyId = 1L; 
        Set<String> notAvailableAccreditationEmissionTradingSchemes = Set.of();
        Account account = mock(Account.class);

        //invoke
        service.unappointAccountsAppointedToVerificationBodyForEmissionTradingSchemes(verificationBodyId, notAvailableAccreditationEmissionTradingSchemes);
        
        verifyNoInteractions(accountRepository, accountVbSiteContactService, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(any());
    }

    @Test
    void unappointAccountsAppointedToVerificationBody() {
        Long accountId = 1L;
        Long verificationBodyId = 1L;
        Set<Long> verificationBodyIds = Set.of(verificationBodyId);
        Account account = mock(Account.class);
        Set<Account> accountsToBeUnappointed = Set.of(account);

        when(account.getId()).thenReturn(accountId);
        when(accountRepository.findAllByVerificationWithContactsBodyIn(verificationBodyIds)).thenReturn(accountsToBeUnappointed);

        // Invoke
        service.unappointAccountsAppointedToVerificationBody(verificationBodyIds);

        // Assert
        verify(accountRepository, times(1)).findAllByVerificationWithContactsBodyIn(verificationBodyIds);
        verify(accountVbSiteContactService, times(1)).removeVbSiteContactFromAccounts(accountsToBeUnappointed);
        verify(eventPublisher, times(1))
            .publishEvent(AccountsVerificationBodyUnappointedEvent.builder()
                .accountIds(Set.of(accountId))
                .build()
            );
        verify(account, times(1)).setVerificationBodyId(null);
        verify(accountVerificationBodyNotificationService, times(1)).notifyUsersForVerificationBodyUnappointment(accountsToBeUnappointed);
    }

    @Test
    void unappointAccountsAppointedToVerificationBody_no_accounts() {
        Long verificationBodyId = 1L;
        Set<Long> verificationBodyIds = Set.of(verificationBodyId);
        Set<Account> accountsToBeUnappointed = Set.of();

        // Mock
        when(accountRepository.findAllByVerificationWithContactsBodyIn(verificationBodyIds)).thenReturn(accountsToBeUnappointed);

        // Invoke
        service.unappointAccountsAppointedToVerificationBody(verificationBodyIds);

        // Assert
        verify(accountRepository, times(1)).findAllByVerificationWithContactsBodyIn(verificationBodyIds);
        verify(accountRepository, never()).save(Mockito.any(Account.class));
        verify(accountVbSiteContactService, never()).removeVbSiteContactFromAccounts(anySet());
        verifyNoInteractions(accountVerificationBodyNotificationService);
    }

    @Test
    void unappointSingleAccountAppointedToVerificationBodyByAccountId() {
        Long accountId = 1L;
        Account account = Mockito.mock(Account.class);
        Set<Account> accountsToBeUnappointed = Set.of(account);

        when(account.getId()).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Invoke
        service.unappointAccountAppointedToVerificationBody(accountId);

        // Assert
        verify(accountRepository).findById(accountId);
        verify(accountVbSiteContactService).removeVbSiteContactFromAccounts(accountsToBeUnappointed);
        verify(accountVerificationBodyNotificationService, times(1)).notifyUsersForVerificationBodyUnappointment(accountsToBeUnappointed);
        verify(eventPublisher)
                .publishEvent(AccountsVerificationBodyUnappointedEvent.builder()
                        .accountIds(Set.of(accountId))
                        .build()
                );
        verify(account, times(1)).setVerificationBodyId(null);
    }

    @Test
    void unappointSingleAccountAppointedToVerificationBodyByAccountId_no_account() {
        Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                service.unappointAccountAppointedToVerificationBody(accountId));

        // Assert
        verify(accountRepository, times(1)).findById(accountId);
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        verifyNoInteractions(accountVbSiteContactService, eventPublisher, accountVerificationBodyNotificationService);
    }
}
