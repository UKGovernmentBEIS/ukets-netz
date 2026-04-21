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
    private final AccountQueryService accountQueryService;

    public void createNewVerificationBodySystemMessage(Long verificationBodyId, Long accountId) {
        Account account = accountQueryService.getAccountById(accountId);
        List<String> verifierAdmins = verifierAuthorityQueryService.findVerifierAdminsByVerificationBody(verificationBodyId);
        verifierAdmins
            .forEach(ver -> systemNotificationProcessAndSendService.processAndSend(
            		buildNewVerificationBodySystemNotificationInfo(account, ver)));
    }

    public void createVerificationBodyNoLongerAvailableSystemMessage(Set<Long> accountIds) {
        accountIds
            .forEach(accountId -> {
                Account acc = accountQueryService.getAccountById(accountId);
                List<String> operatorAdmins = operatorAuthorityQueryService.findActiveOperatorAdminUsersByAccount(acc.getId());
                operatorAdmins.forEach(op ->
                systemNotificationProcessAndSendService.processAndSend(
                		buildVerificationBodyNoLongerAvailableSystemMessageInfo(acc, op)));
            });
    }

    private SystemNotificationInfo buildNewVerificationBodySystemNotificationInfo(Account account, String verifierAdmin) {
        return SystemNotificationInfo.builder()
                .template(NotificationTemplateName.NEW_VERIFICATION_BODY)
                .parameters(Map.of(
                        "emitterName", account.getName(),
                        "emitterId", account.getBusinessId()))
                .accountId(account.getId())
                .receiver(verifierAdmin)
                .build();
    }
    
    private SystemNotificationInfo buildVerificationBodyNoLongerAvailableSystemMessageInfo(
            Account account, String operatorAdmin) {
        return SystemNotificationInfo.builder()
                .template(NotificationTemplateName.VERIFICATION_BODY_NO_LONGER_AVAILABLE)
                .parameters(Map.of("accountId", account.getId()))
                .accountId(account.getId())
                .receiver(operatorAdmin)
                .build();
    }
}
