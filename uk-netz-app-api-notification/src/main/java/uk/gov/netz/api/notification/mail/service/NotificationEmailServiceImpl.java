package uk.gov.netz.api.notification.mail.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.notificationapi.mail.domain.Email;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailRecipients;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;
import uk.gov.netz.api.notificationapi.mail.service.NotificationEmailService;
import uk.gov.netz.api.notificationapi.mail.service.SendEmailService;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notification.template.DownloadLinkMethod;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.ATTACHMENT_NAMES;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.DOWNLOAD_LINK;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.FILE_LINKS;

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
    private final ObjectProvider<EmailLinkedFileStorageService> linkedFileStorageServiceProvider;

    /** Preserves direct construction for callers that do not supply linked files. */
    public NotificationEmailServiceImpl(SendEmailService sendEmailService,
                                        NotificationTemplateProcessService notificationTemplateProcessService,
                                        NotificationProperties notificationProperties) {
        this(sendEmailService, notificationTemplateProcessService, notificationProperties,
                new StaticListableBeanFactory().getBeanProvider(EmailLinkedFileStorageService.class));
    }

    @Autowired
    public NotificationEmailServiceImpl(SendEmailService sendEmailService,
                                        NotificationTemplateProcessService notificationTemplateProcessService,
                                        NotificationProperties notificationProperties,
                                        ObjectProvider<EmailLinkedFileStorageService> linkedFileStorageServiceProvider) {
        this.sendEmailService = sendEmailService;
        this.notificationTemplateProcessService = notificationTemplateProcessService;
        this.notificationProperties = notificationProperties;
        this.linkedFileStorageServiceProvider = linkedFileStorageServiceProvider;
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
        Map<String, Object> templateParams = createTemplateParams(emailData);
        final NotificationContent emailNotificationContent =
                notificationTemplateProcessService.processEmailNotificationTemplate(
                        notificationTemplateData.getTemplateName(),
                        notificationTemplateData.getCompetentAuthority(),
                        templateParams);
        String emailText = createEmailText(emailNotificationContent);

        Email email = Email.builder()
                .from(notificationProperties.getEmail().getAutoSender())
                .recipients(EmailRecipients.builder()
                        .to(recipientsEmails)
                        .cc(ccRecipientsEmails)
                        .bcc(bccRecipientsEmails)
                        .build())
                .subject(emailNotificationContent.getSubject())
                .text(emailText)
                .attachments(emailData.getAttachments())
                .build();

        //send the email
        CompletableFuture.runAsync(() -> sendEmailService.sendMail(email));
    }

    protected String createEmailText(NotificationContent notificationContent) {
        return notificationContent.getText();
    }

    private EmailLinkedFileStorageService getLinkedFileStorageService() {
        List<EmailLinkedFileStorageService> storageServices = linkedFileStorageServiceProvider.stream().toList();
        if (storageServices.isEmpty()) {
            throw new IllegalStateException(
                    "No EmailLinkedFileStorageService provider is configured for email linked-file storage");
        }
        if (storageServices.size() > 1) {
            throw new IllegalStateException(
                    "Multiple EmailLinkedFileStorageService providers are configured; exactly one is required");
        }
        return storageServices.getFirst();
    }

    private Map<String, Object> createTemplateParams(EmailData<EmailNotificationTemplateData> emailData) {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> suppliedParams = emailData.getNotificationTemplateData().getTemplateParams();
        if (suppliedParams != null) {
            params.putAll(suppliedParams);
        }
        Map<String, byte[]> linkedFiles = emailData.getLinkedFiles();
        boolean hasLinkedFiles = linkedFiles != null && !linkedFiles.isEmpty();
        List<EmailFileLink> fileLinks = !hasLinkedFiles ? List.of()
                : getLinkedFileStorageService().storeFiles(linkedFiles).stream()
                        .sorted(Comparator.comparing(EmailFileLink::getFileName)
                                .thenComparing(file -> file.getUrl().toString()))
                        .toList();
        if (hasLinkedFiles || !params.containsKey(FILE_LINKS)) {
            params.put(FILE_LINKS, fileLinks);
        }
        if (hasLinkedFiles || !params.containsKey(ATTACHMENT_NAMES)) {
            params.put(ATTACHMENT_NAMES, emailData.getAttachments() == null ? List.of()
                    : emailData.getAttachments().keySet().stream().sorted().toList());
        }
        if (hasLinkedFiles || !params.containsKey(DOWNLOAD_LINK)) {
            params.put(DOWNLOAD_LINK, new DownloadLinkMethod());
        }
        return params;
    }

}
