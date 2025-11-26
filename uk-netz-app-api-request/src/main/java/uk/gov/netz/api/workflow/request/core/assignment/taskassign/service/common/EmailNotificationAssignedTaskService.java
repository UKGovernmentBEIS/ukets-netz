package uk.gov.netz.api.workflow.request.core.assignment.taskassign.service.common;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.config.WebAppProperties;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.userinfoapi.UserInfoApi;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;
import uk.gov.netz.api.workflow.utils.NotificationTemplateConstants;
import uk.gov.netz.api.workflow.utils.NotificationTemplateName;

import java.util.Collections;
import java.util.Map;

import static java.util.Map.entry;

@Log4j2
@Service
@AllArgsConstructor
public class EmailNotificationAssignedTaskService {

    private final NotificationEmailService<EmailNotificationTemplateData> notificationEmailService;
    private final UserInfoApi userInfoApi;
    private final WebAppProperties webAppProperties;

    /**
     * Sends an email notification to the specified recipient.
     * This method retrieves the user's information based on the provided {@code userId}, constructs the email template
     * data, and sends the email to the recipient using the {@link NotificationEmailService}.
     *
     * @param userId the unique identifier of the recipient user. {@link String}.
     */
    public void sendEmailToRecipient(String userId) {
        if (userId == null) {
            log.error("The userId cannot be null.");
            return;
        }
        UserInfoDTO userInfoDTO = userInfoApi.getUserByUserId(userId);

        notificationEmailService.notifyRecipient(
            EmailData.builder()
                .notificationTemplateData(constructEmailTemplateData(webAppProperties.getUrl()))
                .attachments(Collections.emptyMap())
                .build(),
            userInfoDTO.getEmail()
        );
    }

    private EmailNotificationTemplateData constructEmailTemplateData(String homePage) {
        return EmailNotificationTemplateData.builder()
            .templateName(NotificationTemplateName.EMAIL_ASSIGNED_TASK)
            .templateParams(
                Map.ofEntries(
                    entry(NotificationTemplateConstants.HOME_URL, homePage)))
            .build();
    }
}
