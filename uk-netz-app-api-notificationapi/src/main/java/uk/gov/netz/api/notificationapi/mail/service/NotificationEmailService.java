package uk.gov.netz.api.notificationapi.mail.service;

import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;

import java.util.List;

public interface NotificationEmailService<T extends EmailNotificationTemplateData> {

    void notifyRecipient(EmailData<T> emailData, String recipientEmail);

    void notifyRecipients(EmailData<T> emailData, List<String> recipientsEmails);

    void notifyRecipients(EmailData<T> emailData, List<String> recipientsEmails, List<String> ccRecipientsEmails);

    void notifyRecipients(EmailData<T> emailData, List<String> recipientsEmails, List<String> ccRecipientsEmails,
                          List<String> bccRecipientsEmails);
}
