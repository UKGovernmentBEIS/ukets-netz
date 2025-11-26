package uk.gov.netz.api.account.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityQueryService;
import uk.gov.netz.api.authorization.verifier.service.VerifierAuthorityQueryService;
import uk.gov.netz.api.notification.system.SystemNotificationProcessAndSendService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;
import uk.gov.netz.api.verificationbody.NotificationTemplateName;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountVerificationBodyNotificationServiceTest {

    @InjectMocks
    private AccountVerificationBodyNotificationService service;

    @Mock
    private SystemNotificationProcessAndSendService systemNotificationProcessAndSendService;

    @Mock
    private VerifierAuthorityQueryService verifierAuthorityQueryService;

    @Mock
    private OperatorAuthorityQueryService operatorAuthorityQueryService;

    @Test
    void notifyUsersForVerificationBodyAppointment() {
        Long verificationBodyId = 1L;
        Long accountId = 25L;
        String accountName = "accountName";
        String verifierAdmin = "veradmin";
        String emitterId = "emitterId";
        Account account = Mockito.mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(account.getName()).thenReturn(accountName);
        when(account.getBusinessId()).thenReturn(emitterId);

        when(verifierAuthorityQueryService.findVerifierAdminsByVerificationBody(verificationBodyId))
            .thenReturn(List.of(verifierAdmin));

        //invoke
        service.notifyUsersForVerificationBodyAppointment(verificationBodyId, account);

        verify(verifierAuthorityQueryService, times(1)).findVerifierAdminsByVerificationBody(verificationBodyId);
        ArgumentCaptor<SystemNotificationInfo> messageCaptor = ArgumentCaptor.forClass(SystemNotificationInfo.class);
        verify(systemNotificationProcessAndSendService, times(1)).processAndSend(messageCaptor.capture());
        SystemNotificationInfo message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(NotificationTemplateName.NEW_VERIFICATION_BODY);
        assertThat(message.getAccountId()).isEqualTo(account.getId());
        assertThat(message.getReceiver()).isEqualTo(verifierAdmin);
        assertThat(message.getParameters())
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "emitterName", account.getName(),
                "emitterId", account.getBusinessId()));
    }

    @Test
    void notifyUsersForVerificationBodyUnappointment() {
        Long accountId = 1L;
        String operatorAdmin = "opAdmin";
        List<String> operatorAdmins = List.of(operatorAdmin);
        Account account = Mockito.mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        Set<Account> accountsUnappointed = Set.of(account);

        when(operatorAuthorityQueryService.findActiveOperatorAdminUsersByAccount(accountId)).thenReturn(operatorAdmins);

        //invoke
        service.notifyUsersForVerificationBodyUnappointment(accountsUnappointed);

        verify(operatorAuthorityQueryService, times(1)).findActiveOperatorAdminUsersByAccount(accountId);

        ArgumentCaptor<SystemNotificationInfo> messageCaptor = ArgumentCaptor.forClass(
            SystemNotificationInfo.class);
        verify(systemNotificationProcessAndSendService, times(1)).processAndSend(messageCaptor.capture());
        SystemNotificationInfo message = messageCaptor.getValue();
        assertThat(message.getTemplate()).isEqualTo(NotificationTemplateName.VERIFICATION_BODY_NO_LONGER_AVAILABLE);
        assertThat(message.getReceiver()).isEqualTo(operatorAdmin);
        assertThat(message.getParameters())
            .containsExactlyInAnyOrderEntriesOf(Map.of(
                "accountId", account.getId()));
    }
}
