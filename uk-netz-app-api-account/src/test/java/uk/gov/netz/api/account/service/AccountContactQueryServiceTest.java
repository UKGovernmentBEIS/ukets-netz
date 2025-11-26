package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.account.domain.AccountContactType;
import uk.gov.netz.api.account.repository.AccountRepository;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountContactQueryServiceTest {

    @InjectMocks
    private AccountContactQueryService service;

    @Mock
    private AccountRepository accountRepository;

    @Test
    void findContactByAccountAndContactType() {
        Long accountId = 1L;
        String caSiteContact = "ca";
        Account account = Mockito.mock(Account.class);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(Map.of(AccountContactType.CA_SITE, caSiteContact));

        Optional<String> caSiteContactOpt = service.findContactByAccountAndContactType(accountId, AccountContactType.CA_SITE);

        assertThat(caSiteContactOpt).contains(caSiteContact);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void findContactByAccountAndContactType_no_contact_found() {
        Long accountId = 1L;
        Account account = Mockito.mock(Account.class);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(Map.of(AccountContactType.FINANCIAL, "financial"));

        Optional<String> caSiteContactOpt = service.findContactByAccountAndContactType(accountId, AccountContactType.CA_SITE);

        assertThat(caSiteContactOpt).isEmpty();
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void findPrimaryContactByAccount() {
        Long accountId = 1L;
        String primaryContact = "user";
        Account account = Mockito.mock(Account.class);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(Map.of(AccountContactType.PRIMARY, primaryContact));

        Optional<String> resultOptional = service.findPrimaryContactByAccount(accountId);
        assertThat(resultOptional).isNotEmpty();
        assertEquals(primaryContact, resultOptional.get());
    }

    @Test
    void findPrimaryContactByAccount_not_found() {
        Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThat(service.findPrimaryContactByAccount(accountId)).isEmpty();
    }

    @Test
    void findContactTypesByAccount() {
        Long accountId = 1L;

        Map<String, String> contactTypes =
                Map.of(
                        AccountContactType.PRIMARY, "primary",
                        AccountContactType.SERVICE, "service");

        Account account = Mockito.mock(Account.class);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(contactTypes);

        //invoke
        Map<String, String> result = service.findContactTypesByAccount(accountId);

        assertThat(result).isEqualTo(contactTypes);
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void findContactTypesByAccount_account_not_found() {
        Long accountId = 1L;

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        //invoke
        Map<String, String> result = service.findContactTypesByAccount(accountId);

        assertThat(result).isEqualTo(Map.of());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void findContactTypesByAccount_empty_contacts() {
        Long accountId = 1L;
        Account account = Mockito.mock(Account.class);
        Map<String, String> contactTypes = Map.of();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(contactTypes);

        //invoke
        Map<String, String> result = service.findContactTypesByAccount(accountId);

        assertThat(result).isEqualTo(Map.of());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void findOperatorContactTypesByAccount() {
        Long accountId = 1L;
        Account account = Mockito.mock(Account.class);

        Map<String, String> contactTypes =
                Map.of(AccountContactType.PRIMARY, "primary",
                        AccountContactType.SERVICE, "service",
                        AccountContactType.SECONDARY, "secondary",
                        AccountContactType.FINANCIAL, "financial",
                        AccountContactType.CA_SITE, "ca_site",
                        AccountContactType.VB_SITE, "vb_site");

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(account.getContacts()).thenReturn(contactTypes);

        Map<String, String> result = service.findOperatorContactTypesByAccount(accountId);

        assertThat(result).isEqualTo(Map.of(AccountContactType.PRIMARY, "primary",
                AccountContactType.SECONDARY, "secondary",
                AccountContactType.SERVICE, "service",
                AccountContactType.FINANCIAL, "financial"));
        verify(accountRepository, times(1)).findById(accountId);
    }
}
