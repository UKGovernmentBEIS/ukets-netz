package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.domain.dto.AccountContactDTO;
import uk.gov.netz.api.account.domain.dto.AccountContactVbInfoDTO;
import uk.gov.netz.api.account.domain.dto.AccountContactVbInfoResponse;
import uk.gov.netz.api.account.repository.AccountRepository;
import uk.gov.netz.api.authorization.core.domain.AppAuthority;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.authorization.rules.domain.Scope;
import uk.gov.netz.api.authorization.rules.services.resource.VerificationBodyAuthorizationResourceService;
import uk.gov.netz.api.authorization.rules.services.resource.VerifierAuthorityResourceService;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.common.domain.TestEmissionTradingScheme;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountVbSiteContactServiceTest {

    @InjectMocks
    private AccountVbSiteContactService service;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private VerificationBodyAuthorizationResourceService verificationBodyAuthorizationResourceService;

    @Mock
    private VerifierAuthorityResourceService verifierAuthorityResourceService;

    @Test
    void getAccountsAndVbSiteContacts() {
        final Long vbId = 1L;
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        List<AccountContactVbInfoDTO> contacts = List.of(
            new AccountContactVbInfoDTO(1L, "name", TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME, "userId"));
        Page<AccountContactVbInfoDTO> pagedAccounts = new PageImpl<>(contacts);

        AccountContactVbInfoResponse expected = AccountContactVbInfoResponse.builder()
                .contacts(contacts).editable(true).totalItems(1L).build();

        // Mock
        when(verificationBodyAuthorizationResourceService.hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER))
                .thenReturn(true);
        when(accountRepository
            .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE))
            .thenReturn(pagedAccounts);

        // Invoke
        AccountContactVbInfoResponse actual = service.getAccountsAndVbSiteContacts(user, 0, 1);

        // Assert
        assertEquals(expected, actual);
        verify(verificationBodyAuthorizationResourceService, times(1))
                .hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER);
        verify(accountRepository, times(1))
                .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE);
    }

    @Test
    void getAccountsAndVbSiteContacts_not_editable() {
        final Long vbId = 1L;
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        List<AccountContactVbInfoDTO> contacts = List.of(
            new AccountContactVbInfoDTO(1L, "name", TestEmissionTradingScheme.DUMMY_EMISSION_TRADING_SCHEME, "userId"));
        Page<AccountContactVbInfoDTO> pagedAccounts = new PageImpl<>(contacts);

        AccountContactVbInfoResponse expected = AccountContactVbInfoResponse.builder()
                .contacts(contacts).editable(false).totalItems(1L).build();

        // Mock
        when(verificationBodyAuthorizationResourceService.hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER))
                .thenReturn(false);
        when(accountRepository
            .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE))
            .thenReturn(pagedAccounts);

        // Invoke
        AccountContactVbInfoResponse actual = service.getAccountsAndVbSiteContacts(user, 0, 1);

        // Assert
        assertEquals(expected, actual);
        verify(verificationBodyAuthorizationResourceService, times(1))
                .hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER);
        verify(accountRepository, times(1))
            .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE);
    }


    @Test
    void getAccountsAndVbSiteContacts_no_contacts() {
        final Long vbId = 1L;
        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        Page<AccountContactVbInfoDTO> pagedAccounts = new PageImpl<>(List.of());

        AccountContactVbInfoResponse expected = AccountContactVbInfoResponse.builder()
                .contacts(List.of()).editable(true).totalItems(0L).build();

        // Mock
        when(verificationBodyAuthorizationResourceService.hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER))
                .thenReturn(true);
        when(accountRepository
            .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE))
            .thenReturn(pagedAccounts);

        // Invoke
        AccountContactVbInfoResponse actual = service.getAccountsAndVbSiteContacts(user, 0, 1);

        // Assert
        assertEquals(expected, actual);
        verify(verificationBodyAuthorizationResourceService, times(1))
                .hasUserScopeToVerificationBody(user, vbId, Scope.EDIT_USER);
        verify(accountRepository, times(1))
            .findAccountContactsByVbAndContactType(PageRequest.of(0, 1), vbId, AccountContactType.VB_SITE);
    }

    @Test
    void updateVbSiteContacts() {
        final Long accountId = 1L;
        final String oldUser = "old";
        final String newUser = "new";
        final Long vbId = 1L;

        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        List<AccountContactDTO> vbSiteContactsUpdate = List.of(AccountContactDTO.builder().accountId(accountId).userId(newUser).build());
        Map<String, String> accountContacts = new HashMap<>(){{ put(AccountContactType.VB_SITE, oldUser); }};

        Account account = Mockito.mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getContacts()).thenReturn(accountContacts);

        // Mock
        when(accountRepository.findAllIdsByVB(vbId)).thenReturn(List.of(accountId));
        when(verifierAuthorityResourceService.findUsersByVerificationBodyId(vbId)).thenReturn(List.of(newUser));
        when(accountRepository.findAllByIdIn(List.of(accountId))).thenReturn(List.of(account));

        // Invoke
        service.updateVbSiteContacts(user, vbSiteContactsUpdate);

        // Assert
        assertEquals(account.getContacts().get(AccountContactType.VB_SITE), newUser);

        verify(accountRepository, times(1)).findAllIdsByVB(vbId);
        verify(accountRepository, times(1)).findAllByIdIn(List.of(accountId));
        verify(verifierAuthorityResourceService, times(1)).findUsersByVerificationBodyId(vbId);
    }

    @Test
    void updateVbSiteContacts_account_not_in_vb() {
        final Long accountId = 1L;
        final String newUser = "new";
        final Long vbId = 1L;

        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        List<AccountContactDTO> vbSiteContactsUpdate = List.of(AccountContactDTO.builder().accountId(accountId).userId(newUser).build());

        // Mock
        when(accountRepository.findAllIdsByVB(vbId)).thenReturn(List.of());

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                service.updateVbSiteContacts(user, vbSiteContactsUpdate));

        // Assert
        assertEquals(ErrorCode.ACCOUNT_NOT_RELATED_TO_VB, businessException.getErrorCode());

        verify(accountRepository, times(1)).findAllIdsByVB(vbId);
        verifyNoInteractions(verifierAuthorityResourceService);
    }

    @Test
    void updateVbSiteContacts_user_not_in_vb() {
        final Long accountId = 1L;
        final String newUser = "new";
        final Long vbId = 1L;

        final AppUser user = AppUser.builder().roleType(RoleTypeConstants.VERIFIER)
                .authorities(List.of(AppAuthority.builder().verificationBodyId(vbId).build())).build();
        List<AccountContactDTO> vbSiteContactsUpdate = List.of(AccountContactDTO.builder().accountId(accountId).userId(newUser).build());

        // Mock
        when(accountRepository.findAllIdsByVB(vbId)).thenReturn(List.of(accountId));
        when(verifierAuthorityResourceService.findUsersByVerificationBodyId(vbId)).thenReturn(List.of());

        // Invoke
        BusinessException businessException = assertThrows(BusinessException.class, () ->
                service.updateVbSiteContacts(user, vbSiteContactsUpdate));

        // Assert
        assertEquals(ErrorCode.AUTHORITY_USER_NOT_RELATED_TO_VERIFICATION_BODY, businessException.getErrorCode());

        verify(accountRepository, times(1)).findAllIdsByVB(vbId);
        verify(verifierAuthorityResourceService, times(1)).findUsersByVerificationBodyId(vbId);
    }
    
    @Test
    void removeVbSiteContactFromAccounts() {
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.VB_SITE, "vb_site");

        Account account = Mockito.mock(Account.class);
        when(account.getContacts()).thenReturn(contacts);
        
        service.removeVbSiteContactFromAccounts(Set.of(account));
        
        assertThat(account.getContacts().get(AccountContactType.VB_SITE)).isNull();
    }
    
    @Test
    void removeVbSiteContactFromAccounts_no_nb_site_contained() {
        Map<String, String> contacts = new HashMap<>();
        contacts.put(AccountContactType.CA_SITE, "ca_site");

        Account account = Mockito.mock(Account.class);
        when(account.getContacts()).thenReturn(contacts);
        
        service.removeVbSiteContactFromAccounts(Set.of(account));
        
        assertThat(account.getContacts()).containsEntry(AccountContactType.CA_SITE, "ca_site");
    }
}
