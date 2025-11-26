package uk.gov.netz.api.workflow.request.flow.rfi.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.common.constants.ExpirationReminderType;
import uk.gov.netz.api.workflow.request.flow.common.constants.NotificationTemplateWorkflowTaskType;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestAccountContactQueryService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestExpirationReminderService;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.NotificationTemplateExpirationReminderParams;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class RfiSendReminderNotificationService {

    private final RequestService requestService;
    private final RequestAccountContactQueryService requestAccountContactQueryService;
    private final RequestExpirationReminderService requestExpirationReminderService;

    public void sendFirstReminderNotification(String requestId, Date expirationDate) {
        sendReminderNotification(requestId, expirationDate, ExpirationReminderType.FIRST_REMINDER);
    }

    public void sendSecondReminderNotification(String requestId, Date expirationDate) {
        sendReminderNotification(requestId, expirationDate, ExpirationReminderType.SECOND_REMINDER);
    }
    
    private void sendReminderNotification(String requestId, Date expirationDate, ExpirationReminderType expirationReminderType) {
        final Request request = requestService.findRequestById(requestId);
        
        final UserInfoDTO accountPrimaryContact = requestAccountContactQueryService.getRequestAccountPrimaryContact(request)
            .orElseThrow(() -> new BusinessException(ErrorCode.ACCOUNT_CONTACT_TYPE_PRIMARY_CONTACT_NOT_FOUND));
        requestExpirationReminderService.sendExpirationReminderNotification(requestId, 
                NotificationTemplateExpirationReminderParams.builder()
                    .workflowTask(NotificationTemplateWorkflowTaskType.getDescription(NotificationTemplateWorkflowTaskType.RFI))
                    .recipient(accountPrimaryContact)
                    .expirationTime(expirationReminderType.getDescription())
                    .expirationTimeLong(expirationReminderType.getDescriptionLong())
                    .deadline(expirationDate)
                    .build());
    }
}
