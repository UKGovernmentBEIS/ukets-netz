package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.event.AccountVerificationBodyAppointedEvent;
import uk.gov.netz.api.common.domain.EmissionTradingScheme;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.verificationbody.service.VerificationBodyQueryService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountVerificationBodyAppointServiceTest {

    @InjectMocks
    private AccountVerificationBodyAppointService service;
    
    @Mock
    private AccountQueryService accountQueryService;

    @Mock
    private ApprovedAccountQueryService approvedAccountQueryService;
    
    @Mock
    private VerificationBodyQueryService verificationBodyQueryService;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AccountVerificationBodyNotificationService accountVerificationBodyNotificationService;

    @Test
    void appointVerificationBodyToAccount() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getEmissionTradingScheme()).thenReturn(mock(EmissionTradingScheme.class));
        
        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);

        when(accountQueryService.getAccountVerificationBodyId(accountId))
            .thenReturn(Optional.empty());
        
        when(verificationBodyQueryService.existsActiveVerificationBodyById(verificationBodyId))
            .thenReturn(true);
        
        when(verificationBodyQueryService.isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, account.getEmissionTradingScheme()))
            .thenReturn(true);

        //invoke
        service.appointVerificationBodyToAccount(verificationBodyId, accountId);

        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(accountQueryService, times(1)).getAccountVerificationBodyId(accountId);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(verificationBodyId);
        verify(verificationBodyQueryService, times(1)).isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, account.getEmissionTradingScheme());
        verify(account, times(1)).setVerificationBodyId(verificationBodyId);
        verify(accountVerificationBodyNotificationService, times(1))
            .notifyUsersForVerificationBodyAppointment(verificationBodyId, account);
        verify(eventPublisher, times(1)).publishEvent(AccountVerificationBodyAppointedEvent.builder()
                                .accountId(accountId)
                                .verificationBodyId(verificationBodyId).build());
    }
    
    @Test
    void appointVerificationBodyToAccount_vb_not_found() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);

        when(accountQueryService.getAccountVerificationBodyId(accountId))
            .thenReturn(Optional.empty());
        
        when(verificationBodyQueryService.existsActiveVerificationBodyById(verificationBodyId))
            .thenReturn(false);
        
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.appointVerificationBodyToAccount(verificationBodyId, accountId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);

        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(accountQueryService, times(1)).getAccountVerificationBodyId(accountId);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(verificationBodyId);
        verifyNoMoreInteractions(verificationBodyQueryService);
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }
    
    @Test
    void appointVerificationBodyToAccount_account_already_appointed() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);

        when(accountQueryService.getAccountVerificationBodyId(accountId))
            .thenReturn(Optional.of(verificationBodyId));
        
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.appointVerificationBodyToAccount(verificationBodyId, accountId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.VERIFICATION_BODY_ALREADY_APPOINTED_TO_ACCOUNT);

        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verifyNoMoreInteractions(accountQueryService);
        verifyNoInteractions(verificationBodyQueryService, eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }
    
    @Test
    void appointVerificationBodyToAccount_account_not_approved() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        Account account = mock(Account.class);

        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(false);

        BusinessException be = assertThrows(BusinessException.class, () ->
                service.appointVerificationBodyToAccount(verificationBodyId, accountId));
        
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_INVALID_STATUS);
        
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verifyNoMoreInteractions(accountQueryService);
        verifyNoInteractions(verificationBodyQueryService, eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }
    
    @Test
    void appointVerificationBodyToAccount_vb_not_accredited_to_account_emission_scheme() {
        Long verificationBodyId = 1L;
        Long accountId = 1L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);

        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);

        when(accountQueryService.getAccountVerificationBodyId(accountId))
            .thenReturn(Optional.empty());
        
        when(verificationBodyQueryService.existsActiveVerificationBodyById(verificationBodyId))
            .thenReturn(true);
        
        when(verificationBodyQueryService.isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, account.getEmissionTradingScheme()))
            .thenReturn(false);
        
        BusinessException be = assertThrows(BusinessException.class, () ->
                service.appointVerificationBodyToAccount(verificationBodyId, accountId));
        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.VERIFICATION_BODY_NOT_ACCREDITED_TO_ACCOUNTS_EMISSION_TRADING_SCHEME);
        
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(accountQueryService, times(1)).getAccountVerificationBodyId(accountId);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(verificationBodyId);
        verify(verificationBodyQueryService, times(1)).isVerificationBodyAccreditedToEmissionTradingScheme(verificationBodyId, account.getEmissionTradingScheme());
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }

    @Test
    void replaceVerificationBodyToAccount() {
        final Long accountId = 1L;
        final Long currentVerificationBodyId = 1L;
        final Long newVerificationBodyId = 2L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getVerificationBodyId()).thenReturn(currentVerificationBodyId);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(verificationBodyQueryService.existsActiveVerificationBodyById(newVerificationBodyId)).thenReturn(true);
        when(verificationBodyQueryService.isVerificationBodyAccreditedToEmissionTradingScheme(newVerificationBodyId, account.getEmissionTradingScheme()))
            .thenReturn(true);
        
        // Invoke
        service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId);

        // Assert
        assertTrue(account.getContacts().isEmpty());
        verify(account, times(1)).setVerificationBodyId(newVerificationBodyId);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(newVerificationBodyId);
        verify(verificationBodyQueryService, times(1)).isVerificationBodyAccreditedToEmissionTradingScheme(newVerificationBodyId, account.getEmissionTradingScheme());
        verify(accountVerificationBodyNotificationService, times(1))
            .notifyUsersForVerificationBodyAppointment(newVerificationBodyId, account);
        verify(eventPublisher, times(1))
            .publishEvent(AccountVerificationBodyAppointedEvent.builder()
                .accountId(accountId)
                .verificationBodyId(newVerificationBodyId)
                .build());
    }

    @Test
    void replaceVerificationBodyToAccount_account_not_approved() {
        final Long accountId = 1L;
        final Long newVerificationBodyId = 2L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId))
            .thenReturn(account);

        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(false);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId));

        // Assert
        assertEquals(ErrorCode.ACCOUNT_INVALID_STATUS, businessException.getErrorCode());
        assertThat(account.getContacts()).containsExactlyInAnyOrderEntriesOf(contacts);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verifyNoInteractions(verificationBodyQueryService, eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }

    @Test
    void replaceVerificationBodyToAccount_no_vb() {
        final Long accountId = 1L;
        final Long newVerificationBodyId = 2L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(verificationBodyQueryService.existsActiveVerificationBodyById(newVerificationBodyId)).thenReturn(false);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId));

        // Assert
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, businessException.getErrorCode());
        assertThat(account.getContacts()).containsExactlyInAnyOrderEntriesOf(contacts);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(newVerificationBodyId);
        verifyNoMoreInteractions(verificationBodyQueryService);
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }

    @Test
    void replaceVerificationBodyToAccount_appoint_same_vb() {
        final Long accountId = 1L;
        final Long newVerificationBodyId = 1L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getVerificationBodyId()).thenReturn(newVerificationBodyId);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(verificationBodyQueryService.existsActiveVerificationBodyById(newVerificationBodyId)).thenReturn(true);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId));

        // Assert
        assertEquals(ErrorCode.VERIFICATION_BODY_ALREADY_APPOINTED_TO_ACCOUNT, businessException.getErrorCode());
        assertThat(account.getContacts()).containsExactlyInAnyOrderEntriesOf(contacts);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(newVerificationBodyId);
        verifyNoMoreInteractions(verificationBodyQueryService);
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }

    @Test
    void replaceVerificationBodyToAccount_no_vb_appointment() {
        final Long accountId = 1L;
        final Long newVerificationBodyId = 2L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getVerificationBodyId()).thenReturn(null);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(verificationBodyQueryService.existsActiveVerificationBodyById(newVerificationBodyId)).thenReturn(true);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId));

        // Assert
        assertEquals(ErrorCode.VERIFICATION_BODY_NOT_APPOINTED_TO_ACCOUNT, businessException.getErrorCode());
        assertThat(account.getContacts()).containsExactlyInAnyOrderEntriesOf(contacts);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(newVerificationBodyId);
        verifyNoMoreInteractions(verificationBodyQueryService);
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }
    
    @Test
    void replaceVerificationBodyToAccount_vb_not_accredited_to_account_emission_scheme() {
        final Long accountId = 1L;
        final Long currentVerificationBodyId = 1L;
        final Long newVerificationBodyId = 2L;
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "userId");
        Account account = mock(Account.class);
        when(account.getVerificationBodyId()).thenReturn(currentVerificationBodyId);
        when(account.getContacts()).thenReturn(contacts);

        when(accountQueryService.getAccountById(accountId)).thenReturn(account);
        when(approvedAccountQueryService.isAccountApproved(account)).thenReturn(true);
        when(verificationBodyQueryService.existsActiveVerificationBodyById(newVerificationBodyId)).thenReturn(true);
        when(verificationBodyQueryService.isVerificationBodyAccreditedToEmissionTradingScheme(newVerificationBodyId, account.getEmissionTradingScheme()))
            .thenReturn(false);

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class,
                () -> service.replaceVerificationBodyToAccount(newVerificationBodyId, accountId));

        // Assert
        assertEquals(ErrorCode.VERIFICATION_BODY_NOT_ACCREDITED_TO_ACCOUNTS_EMISSION_TRADING_SCHEME, businessException.getErrorCode());
        assertThat(account.getContacts()).containsExactlyInAnyOrderEntriesOf(contacts);
        verify(accountQueryService, times(1)).getAccountById(accountId);
        verify(approvedAccountQueryService, times(1)).isAccountApproved(account);
        verify(verificationBodyQueryService, times(1)).existsActiveVerificationBodyById(newVerificationBodyId);
        verify(verificationBodyQueryService, times(1)).isVerificationBodyAccreditedToEmissionTradingScheme(newVerificationBodyId, account.getEmissionTradingScheme());
        verifyNoInteractions(eventPublisher, accountVerificationBodyNotificationService);
        verify(account, never()).setVerificationBodyId(anyLong());
    }
}
