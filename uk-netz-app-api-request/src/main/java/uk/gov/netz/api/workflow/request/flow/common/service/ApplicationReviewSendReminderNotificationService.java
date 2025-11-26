package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.common.constants.ExpirationReminderType;
import uk.gov.netz.api.workflow.request.flow.common.constants.NotificationTemplateWorkflowTaskType;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.NotificationTemplateExpirationReminderParams;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApplicationReviewSendReminderNotificationService {
    
    private final RequestService requestService;
    private final UserInfoApi userInfoApi;
    private final RequestExpirationReminderService requestExpirationReminderService;

    public void sendFirstReminderNotification(String requestId, Date deadline) {
        sendReminderNotification(requestId, deadline, ExpirationReminderType.FIRST_REMINDER);
    }
    
    public void sendSecondReminderNotification(String requestId, Date deadline) {
        sendReminderNotification(requestId, deadline, ExpirationReminderType.SECOND_REMINDER);
    }
    
    private void sendReminderNotification(String requestId, Date deadline, ExpirationReminderType expirationType) {
        final Request request = requestService.findRequestById(requestId);
        final String regulatorAssignee = request.getPayload().getRegulatorAssignee();
        if (regulatorAssignee == null) {
            return;
        }
        
        UserInfoDTO regulatorAssigneeUser = userInfoApi.getUserByUserId(regulatorAssignee);
        
        requestExpirationReminderService.sendExpirationReminderNotification(requestId, 
                NotificationTemplateExpirationReminderParams.builder()
                    .workflowTask(NotificationTemplateWorkflowTaskType.getDescription(request.getType().getCode()))
                    .recipient(regulatorAssigneeUser)
                    .expirationTime(expirationType.getDescription())
                    .expirationTimeLong(expirationType.getDescriptionLong())
                    .deadline(deadline)
                    .build());
    }
}
