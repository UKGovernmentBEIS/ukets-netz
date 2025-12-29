package uk.gov.netz.api.notification.mail.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.notificationapi.mail.domain.Email;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailRecipients;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.notificationapi.mail.service.SendEmailService;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service implementation for generating and sending email notifications
 */
@Log4j2
@Service
@ConditionalOnProperty(name = "env.isProd", havingValue = "true")
public class NotificationEmailServiceImpl implements NotificationEmailService<EmailNotificationTemplateData> {

    private final SendEmailService sendEmailService;
    private final NotificationTemplateProcessService notificationTemplateProcessService;
    private final NotificationProperties notificationProperties;

    public NotificationEmailServiceImpl(SendEmailService sendEmailService,
                                        NotificationTemplateProcessService notificationTemplateProcessService,
                                        NotificationProperties notificationProperties) {
        this.sendEmailService = sendEmailService;
        this.notificationTemplateProcessService = notificationTemplateProcessService;
        this.notificationProperties = notificationProperties;
    }

    @Override
    public void notifyRecipient(EmailData<EmailNotificationTemplateData> emailData, String recipientEmail) {
        this.notifyRecipients(emailData, List.of(recipientEmail), Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public void notifyRecipients(EmailData<EmailNotificationTemplateData> emailData, List<String> recipientsEmails) {
        this.notifyRecipients(emailData, recipientsEmails, Collections.emptyList(), Collections.emptyList());
    }

    @Override
    public void notifyRecipients(EmailData<EmailNotificationTemplateData> emailData, List<String> recipientsEmails, List<String> ccRecipientsEmails) {
        this.notifyRecipients(emailData, recipientsEmails, ccRecipientsEmails, Collections.emptyList());
    }

    @Override
    public void notifyRecipients(EmailData<EmailNotificationTemplateData> emailData, List<String> recipientsEmails, List<String> ccRecipientsEmails, List<String> bccRecipientsEmails) {
        EmailNotificationTemplateData notificationTemplateData = emailData.getNotificationTemplateData();
        final NotificationContent emailNotificationContent =
                notificationTemplateProcessService.processEmailNotificationTemplate(
                        notificationTemplateData.getTemplateName(),
                        notificationTemplateData.getCompetentAuthority(),
                        notificationTemplateData.getTemplateParams());

        Email email = Email.builder()
                .from(notificationProperties.getEmail().getAutoSender())
                .recipients(EmailRecipients.builder()
                        .to(recipientsEmails)
                        .cc(ccRecipientsEmails)
                        .bcc(bccRecipientsEmails)
                        .build())
                .subject(emailNotificationContent.getSubject())
                .text(createEmailText(emailNotificationContent))
                .attachments(emailData.getAttachments())
                .build();

        //send the email
        CompletableFuture.runAsync(() -> sendEmailService.sendMail(email));
    }

    protected String createEmailText(NotificationContent notificationContent) {
        return notificationContent.getText();
    }

}
