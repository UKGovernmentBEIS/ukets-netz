package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.account.domain.dto.AccountInfoDTO;
import uk.gov.netz.api.account.service.AccountQueryService;
import uk.gov.netz.api.competentauthority.CompetentAuthorityService;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.common.service.notification.NotificationTemplateExpirationReminderParams;
import uk.gov.netz.api.workflow.utils.NotificationTemplateConstants;
import uk.gov.netz.api.workflow.utils.NotificationTemplateName;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestExpirationReminderService {

    private final RequestService requestService;
    private final AccountQueryService accountQueryService;
    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    private final CompetentAuthorityService competentAuthorityService;
    private final RequestAccountContactQueryService requestAccountContactQueryService;

    public void sendExpirationReminderNotification(String requestId, NotificationTemplateExpirationReminderParams expirationParams) {
        final Request request = requestService.findRequestById(requestId);
        final Long accountId = request.getAccountId();
        final AccountInfoDTO accountInfo = accountQueryService.getAccountInfoDTOById(accountId);
        final Optional<UserInfoDTO> accountPrimaryContact = requestAccountContactQueryService.getRequestAccountPrimaryContact(request);
        final Optional<UserInfoDTO> accountServiceContact = requestAccountContactQueryService.getRequestAccountServiceContact(request);

        final Map<String, Object> templateParams = new HashMap<>();
        templateParams.put(NotificationTemplateConstants.ACCOUNT_NAME, accountInfo.getName());
        templateParams.put(NotificationTemplateConstants.ACCOUNT_BUSINESS_ID, accountInfo.getBusinessId());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_ID, request.getId());
        templateParams.put(NotificationTemplateConstants.WORKFLOW, request.getType().getDescription());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_TASK, expirationParams.getWorkflowTask());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_USER, expirationParams.getRecipient().getFullName());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME, expirationParams.getExpirationTime());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_EXPIRATION_TIME_LONG, expirationParams.getExpirationTimeLong());
        templateParams.put(NotificationTemplateConstants.WORKFLOW_DEADLINE, expirationParams.getDeadline());
        templateParams.put(NotificationTemplateConstants.COMPETENT_AUTHORITY_EMAIL, competentAuthorityService
                .getCompetentAuthorityDTO(request.getCompetentAuthority()).getEmail());

        accountPrimaryContact
            .ifPresent(userInfo -> {
                templateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT, userInfo.getFullName());
                templateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_FIRST_NAME, userInfo.getFirstName());
                templateParams.put(NotificationTemplateConstants.ACCOUNT_PRIMARY_CONTACT_LAST_NAME, userInfo.getLastName());
            });
        accountServiceContact
            .ifPresent(userInfo -> {
                templateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT, userInfo.getFullName());
                templateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_FIRST_NAME, userInfo.getFirstName());
                templateParams.put(NotificationTemplateConstants.ACCOUNT_SERVICE_CONTACT_LAST_NAME, userInfo.getLastName());
            });

        final EmailData<EmailNotificationTemplateData> emailData = EmailData.builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder()
                        .competentAuthority(request.getCompetentAuthority())
                        .templateName(NotificationTemplateName.GENERIC_EXPIRATION_REMINDER)
                        .templateParams(templateParams)
                        .build())
                .build();
        
        notificationEmailService.notifyRecipient(emailData, expirationParams.getRecipient().getEmail());
    }
}
