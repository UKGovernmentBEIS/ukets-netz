package uk.gov.netz.api.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.Account;
import uk.gov.netz.api.authorization.operator.service.OperatorAuthorityQueryService;
import uk.gov.netz.api.authorization.verifier.service.VerifierAuthorityQueryService;
import uk.gov.netz.api.notification.system.SystemNotificationProcessAndSendService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;
import uk.gov.netz.api.verificationbody.NotificationTemplateName;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AccountVerificationBodyNotificationService {
    
	private final SystemNotificationProcessAndSendService systemNotificationProcessAndSendService;
    private final VerifierAuthorityQueryService verifierAuthorityQueryService;
    private final OperatorAuthorityQueryService operatorAuthorityQueryService;

    public void notifyUsersForVerificationBodyAppointment(Long verificationBodyId, Account account) {
        List<String> verifierAdmins = verifierAuthorityQueryService.findVerifierAdminsByVerificationBody(verificationBodyId);
        verifierAdmins
            .forEach(ver -> systemNotificationProcessAndSendService.processAndSend(
            		createNewVerificationBodySystemMessage(account, ver)));
    }
    
    public void notifyUsersForVerificationBodyUnappointment(Set<? extends Account> accountsUnappointed) {
        accountsUnappointed
            .forEach(acc -> {
                List<String> operatorAdmins = operatorAuthorityQueryService.findActiveOperatorAdminUsersByAccount(acc.getId());
                operatorAdmins.forEach(op ->
                systemNotificationProcessAndSendService.processAndSend(
                		createVerificationBodyNoLongerAvailableSystemMessage(acc, op)));
            });
    }
    
    private SystemNotificationInfo createNewVerificationBodySystemMessage(Account account, String verifierAdmin) {
        return SystemNotificationInfo.builder()
                .template(NotificationTemplateName.NEW_VERIFICATION_BODY)
                .parameters(Map.of(
                        "emitterName", account.getName(),
                        "emitterId", account.getBusinessId()))
                .accountId(account.getId())
                .receiver(verifierAdmin)
                .build();
    }
    
    private SystemNotificationInfo createVerificationBodyNoLongerAvailableSystemMessage(
            Account account, String operatorAdmin) {
        return SystemNotificationInfo.builder()
                .template(NotificationTemplateName.VERIFICATION_BODY_NO_LONGER_AVAILABLE)
                .parameters(Map.of("accountId", account.getId()))
                .accountId(account.getId())
                .receiver(operatorAdmin)
                .build();
    }
}
