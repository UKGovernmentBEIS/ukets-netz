package uk.gov.netz.api.notification.mail.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;
import uk.gov.netz.api.notification.template.CustomFreeMarkerConfiguration;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateRepository;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.mail.domain.Email;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;
import uk.gov.netz.api.notificationapi.mail.service.SendEmailService;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.ATTACHMENT_NAMES;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.DOWNLOAD_LINK;
import static uk.gov.netz.api.notificationapi.mail.constants.EmailFileTemplateConstants.FILE_LINKS;

@ExtendWith(MockitoExtension.class)
class NotificationEmailServiceImplTest {

    private static final String RECIPIENT = "recipient@example.test";

    @Mock
    private SendEmailService sender;
    @Mock
    private NotificationTemplateRepository repository;
    @Mock
    private ObjectProvider<EmailLinkedFileStorageService> providers;
    @Mock
    private EmailLinkedFileStorageService storage;

    private NotificationProperties properties;
    private NotificationTemplateProcessService renderer;
    private NotificationEmailServiceImpl service;

    @BeforeEach
    void setUp() {
        properties = new NotificationProperties();
        NotificationProperties.Email email = new NotificationProperties.Email();
        email.setAutoSender("sender@example.test");
        properties.setEmail(email);
        renderer = new NotificationTemplateProcessService(new CustomFreeMarkerConfiguration().freemarkerConfig(), repository);
        service = new NotificationEmailServiceImpl(sender, renderer, properties, providers);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void attachmentOnlyOverloadsPreserveRecipientsAndDoNotResolveStorage(int overload) {
        Map<String, byte[]> attachments = Map.of("notice.pdf", new byte[]{1});
        EmailData<EmailNotificationTemplateData> data = data(attachments, Map.of());
        template("${body}");
        List<String> cc = List.of("cc@example.test");
        List<String> bcc = List.of("bcc@example.test");

        switch (overload) {
            case 0 -> service.notifyRecipient(data, RECIPIENT);
            case 1 -> service.notifyRecipients(data, List.of(RECIPIENT));
            case 2 -> service.notifyRecipients(data, List.of(RECIPIENT), cc);
            default -> service.notifyRecipients(data, List.of(RECIPIENT), cc, bcc);
        }

        Email email = capturedEmail();
        assertThat(email.getFrom()).isEqualTo("sender@example.test");
        assertThat(email.getSubject()).isEqualTo("Notice");
        assertThat(email.getText()).isEqualTo("<p>Body</p>\n");
        assertThat(email.getAttachments()).isSameAs(attachments);
        assertThat(email.getRecipients().getTo()).containsExactly(RECIPIENT);
        assertThat(email.getRecipients().getCc()).isEqualTo(overload >= 2 ? cc : List.of());
        assertThat(email.getRecipients().getBcc()).isEqualTo(overload == 3 ? bcc : List.of());
        verifyNoInteractions(providers, storage);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void nullAndEmptyLinkedFilesExposeEmptyCollectionsWithoutStorage(boolean nullFiles) {
        EmailData<EmailNotificationTemplateData> data = data(Map.of(), nullFiles ? null : Map.of());
        template("<#if fileLinks?has_content>links</#if><#if attachmentNames?has_content>attachments</#if>Body");

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).isEqualTo("<p>Body</p>\n");
        verifyNoInteractions(providers, storage);
    }

    @Test
    void templatePlacesSortedLinksBetweenItsOwnParagraphs() {
        EmailData<EmailNotificationTemplateData> data = data(Map.of(), Map.of("a", new byte[]{1}, "b", new byte[]{2}, "z", new byte[]{3}));
        template("""
                Before the downloads.

                <#list fileLinks as file>
                - ${downloadLink(file)}
                </#list>

                After the downloads.
                """);
        stored(data, List.of(file("zeta.pdf", "https://files.example.test/zeta"),
                file("alpha & <report>.pdf", "https://files.example.test/b?x=1&y=2"),
                file("alpha & <report>.pdf", "https://files.example.test/a?x=1&y=2")));

        service.notifyRecipient(data, RECIPIENT);

        Email email = capturedEmail();
        String body = email.getText();
        assertThat(body).contains("<p>Before the downloads.</p>", "<p>After the downloads.</p>",
                "alpha &amp; &lt;report&gt;.pdf", "href=\"https://files.example.test/a?x=1&amp;y=2\"");
        assertThat(body.indexOf("Before the downloads.")).isLessThan(body.indexOf("/a?x=1"));
        assertThat(body.indexOf("/a?x=1")).isLessThan(body.indexOf("/b?x=1"));
        assertThat(body.indexOf("/b?x=1")).isLessThan(body.indexOf("/zeta"));
        assertThat(body.indexOf("/zeta")).isLessThan(body.indexOf("After the downloads."));
        assertThat(body).doesNotContain("Files shared with this email:");
        assertThat(email.getAttachments()).isEmpty();
    }

    @Test
    void mixedEmailUsesAttachmentNamesAndCustomLinkLabel() {
        Map<String, byte[]> attachments = Map.of("z.pdf", new byte[]{1}, "a.pdf", new byte[]{2});
        EmailData<EmailNotificationTemplateData> data = data(attachments, Map.of("linked.pdf", new byte[]{3}));
        template("""
                <#if attachmentNames?has_content>Attached: ${attachmentNames?join(", ")}</#if>

                <#list fileLinks as file>${downloadLink(file, "Download the notice")}</#list>
                """);
        stored(data, List.of(file("linked.pdf", "https://files.example.test/linked")));

        service.notifyRecipient(data, RECIPIENT);

        Email email = capturedEmail();
        assertThat(email.getAttachments()).isSameAs(attachments);
        assertThat(email.getText()).contains("Attached: a.pdf, z.pdf",
                "href=\"https://files.example.test/linked\">Download the notice</a>");
    }

    @Test
    void linkedFilenameCannotInjectHtml() {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        template("<#list fileLinks as file>${downloadLink(file)}</#list>");
        stored(data, List.of(file("\"><img src=x onerror=alert(1)>.pdf", "https://files.example.test/id?a=1&b=2")));

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).contains("&lt;img src=x onerror=alert(1)&gt;.pdf",
                "href=\"https://files.example.test/id?a=1&amp;b=2\"").doesNotContain("<img");
    }

    @Test
    void disclaimerPrecedesTheCompleteTemplateRenderedBody() {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        template("<#list fileLinks as file>${downloadLink(file)}</#list>\n\nClosing text.");
        stored(data, List.of(file("linked.pdf", "https://files.example.test/linked")));
        NotificationEmailWithDisclaimerServiceImpl nonProduction =
                new NotificationEmailWithDisclaimerServiceImpl(sender, renderer, properties, providers);

        nonProduction.notifyRecipient(data, RECIPIENT);

        String body = capturedEmail().getText();
        assertThat(body).startsWith(NotificationEmailWithDisclaimerServiceImpl.MAIL_DISCLAIMER + System.lineSeparator());
        assertThat(body.indexOf("https://files.example.test/linked")).isLessThan(body.indexOf("Closing text."));
    }

    @Test
    void templateMayOmitLinksWithoutAnAppendedFallback() {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        template("Custom body only.");
        stored(data, List.of(file("linked.pdf", "https://files.example.test/linked")));

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).isEqualTo("<p>Custom body only.</p>\n");
    }

    @Test
    void linkedFilesOverrideCallerValuesWithoutMutatingTheirMap() {
        EmailData<EmailNotificationTemplateData> data = data(Map.of("z.pdf", new byte[]{1}, "a.pdf", new byte[]{2}),
                Map.of("linked.pdf", new byte[]{3}));
        Map<String, Object> supplied = Map.of("body", "Body", FILE_LINKS, "caller links",
                ATTACHMENT_NAMES, "caller names", DOWNLOAD_LINK, "caller helper");
        data.getNotificationTemplateData().setTemplateParams(supplied);
        template("${body}: ${attachmentNames?join(\", \")} <#list fileLinks as file>${downloadLink(file)}</#list>");
        stored(data, List.of(file("linked.pdf", "https://files.example.test/linked")));

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).contains("Body: a.pdf, z.pdf", "href=\"https://files.example.test/linked\"")
                .doesNotContain("caller links", "caller names", "caller helper");
        assertThat(supplied).containsExactlyInAnyOrderEntriesOf(Map.of("body", "Body", FILE_LINKS, "caller links",
                ATTACHMENT_NAMES, "caller names", DOWNLOAD_LINK, "caller helper"));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void legacyEmailsPreserveCallerValuesAndImmutableMaps(boolean nullLinkedFiles) {
        EmailData<EmailNotificationTemplateData> data = data(Map.of("notice.pdf", new byte[]{1}), nullLinkedFiles ? null : Map.of());
        Map<String, Object> supplied = Map.of(FILE_LINKS, "caller links", ATTACHMENT_NAMES, "caller names", DOWNLOAD_LINK, "caller helper");
        data.getNotificationTemplateData().setTemplateParams(supplied);
        template("${fileLinks}; ${attachmentNames}; ${downloadLink}");

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).isEqualTo("<p>caller links; caller names; caller helper</p>\n");
        assertThat(data.getNotificationTemplateData().getTemplateParams()).isSameAs(supplied);
        verifyNoInteractions(providers, storage);
    }

    @ParameterizedTest
    @ValueSource(strings = {FILE_LINKS, ATTACHMENT_NAMES, DOWNLOAD_LINK})
    void legacyEmailsPreserveExplicitNullValues(String parameter) {
        EmailData<EmailNotificationTemplateData> data = data(Map.of("notice.pdf", new byte[]{1}), Map.of());
        Map<String, Object> supplied = new HashMap<>();
        supplied.put(parameter, null);
        data.getNotificationTemplateData().setTemplateParams(supplied);
        template("${" + parameter + "!\"legacy-null\"}");

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).isEqualTo("<p>legacy-null</p>\n");
        assertThat(supplied).containsOnlyKeys(parameter).containsEntry(parameter, null);
        verifyNoInteractions(providers, storage);
    }

    @Test
    void legacyEmailsFillOnlyMissingMetadata() {
        EmailData<EmailNotificationTemplateData> data = data(Map.of("z.pdf", new byte[]{1}, "a.pdf", new byte[]{2}), Map.of());
        data.getNotificationTemplateData().setTemplateParams(Map.of(FILE_LINKS, "caller links",
                "customLink", file("custom.pdf", "https://files.example.test/custom")));
        template("${fileLinks}; ${attachmentNames?join(\", \")}; ${downloadLink(customLink)}");

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).contains("caller links; a.pdf, z.pdf;",
                "href=\"https://files.example.test/custom\">custom.pdf</a>");
        verifyNoInteractions(providers, storage);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void legacyConstructorsSupportAttachmentsAndSubclassing(int implementation) {
        NotificationEmailServiceImpl legacy = legacyService(implementation);
        EmailData<EmailNotificationTemplateData> data = data(Map.of("notice.pdf", new byte[]{1}), Map.of());
        template("${body}");

        legacy.notifyRecipient(data, RECIPIENT);

        Email email = capturedEmail();
        assertThat(email.getAttachments()).isSameAs(data.getAttachments());
        String prefix = switch (implementation) {
            case 1 -> NotificationEmailWithDisclaimerServiceImpl.MAIL_DISCLAIMER + System.lineSeparator();
            case 2 -> "Custom: ";
            default -> "";
        };
        assertThat(email.getText()).isEqualTo(prefix + "<p>Body</p>\n");
        verifyNoInteractions(providers, storage);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void legacyConstructorsFailClearlyWhenLinkedFilesHaveNoProvider(int implementation) {
        assertThatThrownBy(() -> legacyService(implementation).notifyRecipient(linkedData(), RECIPIENT))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No EmailLinkedFileStorageService");

        verifyNoInteractions(repository, sender, providers, storage);
    }

    @ParameterizedTest
    @CsvSource({"true,false", "false,false", "true,true", "false,true"})
    void springSelectsTheProviderAwareConstructor(boolean production, boolean links) {
        EmailData<EmailNotificationTemplateData> data = data(Map.of("notice.pdf", new byte[]{1}),
                links ? Map.of("linked.pdf", new byte[]{2}) : Map.of());
        template("${body}");
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(NotificationEmailServiceImpl.class, NotificationEmailWithDisclaimerServiceImpl.class)
                .withPropertyValues("env.isProd=" + production)
                .withBean(SendEmailService.class, () -> sender)
                .withBean(NotificationTemplateProcessService.class, () -> renderer)
                .withBean(NotificationProperties.class, () -> properties);
        if (links) {
            runner = runner.withBean(EmailLinkedFileStorageService.class, () -> storage);
            when(storage.storeFiles(data.getLinkedFiles())).thenReturn(List.of(file("linked.pdf", "https://files.example.test/linked")));
        }

        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(NotificationEmailServiceImpl.class);
            context.getBean(NotificationEmailServiceImpl.class).notifyRecipient(data, RECIPIENT);
            String prefix = production ? "" : NotificationEmailWithDisclaimerServiceImpl.MAIL_DISCLAIMER + System.lineSeparator();
            assertThat(capturedEmail().getText()).isEqualTo(prefix + "<p>Body</p>\n");
            if (links) {
                verify(storage).storeFiles(data.getLinkedFiles());
            } else {
                verifyNoInteractions(storage);
            }
        });
        verifyNoInteractions(providers);
    }

    @Test
    void nullCallerParametersStillExposeTheFileTemplateModel() {
        EmailData<EmailNotificationTemplateData> data = data(Map.of(), Map.of());
        data.getNotificationTemplateData().setTemplateParams(null);
        template("Files: ${fileLinks?size}, attachments: ${attachmentNames?size}");

        service.notifyRecipient(data, RECIPIENT);

        assertThat(capturedEmail().getText()).isEqualTo("<p>Files: 0, attachments: 0</p>\n");
        verifyNoInteractions(providers, storage);
    }

    @Test
    void missingProviderFailsBeforeTemplateProcessingOrSchedulingEmail() {
        when(providers.stream()).thenReturn(Stream.empty());

        assertThatThrownBy(() -> service.notifyRecipient(linkedData(), RECIPIENT))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No EmailLinkedFileStorageService");

        verifyNoInteractions(repository, storage, sender);
    }

    @Test
    void multipleProvidersFailBeforeTemplateProcessingOrSchedulingEmail() {
        EmailLinkedFileStorageService other = mock(EmailLinkedFileStorageService.class);
        when(providers.stream()).thenReturn(Stream.of(storage, other));

        assertThatThrownBy(() -> service.notifyRecipient(linkedData(), RECIPIENT))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Multiple EmailLinkedFileStorageService");

        verifyNoInteractions(repository, storage, other, sender);
    }

    @Test
    void uploadFailurePropagatesBeforeTemplateProcessingOrSchedulingEmail() {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        when(providers.stream()).thenReturn(Stream.of(storage));
        RuntimeException failure = new IllegalStateException("storage unavailable");
        when(storage.storeFiles(data.getLinkedFiles())).thenThrow(failure);

        assertThatThrownBy(() -> service.notifyRecipient(data, RECIPIENT)).isSameAs(failure);

        verifyNoInteractions(repository, sender);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void templateLookupAndRenderingFailuresLeaveStoredFilesAndDoNotScheduleEmail(boolean missingTemplate) {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        stored(data, List.of(file("linked.pdf", "https://files.example.test/linked")));
        if (missingTemplate) {
            when(repository.findByNameAndCompetentAuthority("test", CompetentAuthorityEnum.ENGLAND)).thenReturn(Optional.empty());
        } else {
            template("${missingParameter}");
        }

        assertThatThrownBy(() -> service.notifyRecipient(data, RECIPIENT)).isInstanceOf(BusinessException.class);

        InOrder order = inOrder(storage, repository);
        order.verify(storage).storeFiles(data.getLinkedFiles());
        order.verify(repository).findByNameAndCompetentAuthority("test", CompetentAuthorityEnum.ENGLAND);
        verifyNoMoreInteractions(storage);
        verifyNoInteractions(sender);
    }

    @Test
    void uploadCompletesBeforeRenderingAndAsynchronousSending() {
        EmailData<EmailNotificationTemplateData> data = linkedData();
        template("<#list fileLinks as file>${downloadLink(file)}</#list>");
        when(providers.stream()).thenReturn(Stream.of(storage));
        when(storage.storeFiles(data.getLinkedFiles())).thenAnswer(invocation -> {
            verifyNoInteractions(repository, sender);
            return List.of(file("linked.pdf", "https://files.example.test/linked"));
        });

        service.notifyRecipient(data, RECIPIENT);

        capturedEmail();
        InOrder order = inOrder(storage, repository);
        order.verify(storage).storeFiles(data.getLinkedFiles());
        order.verify(repository).findByNameAndCompetentAuthority("test", CompetentAuthorityEnum.ENGLAND);
    }

    private Email capturedEmail() {
        ArgumentCaptor<Email> captured = ArgumentCaptor.forClass(Email.class);
        verify(sender, timeout(1000)).sendMail(captured.capture());
        return captured.getValue();
    }

    private NotificationEmailServiceImpl legacyService(int implementation) {
        return switch (implementation) {
            case 1 -> new NotificationEmailWithDisclaimerServiceImpl(sender, renderer, properties);
            case 2 -> new LegacyNotificationSubclass(sender, renderer, properties);
            default -> new NotificationEmailServiceImpl(sender, renderer, properties);
        };
    }

    private static class LegacyNotificationSubclass extends NotificationEmailServiceImpl {

        LegacyNotificationSubclass(SendEmailService sender, NotificationTemplateProcessService renderer, NotificationProperties properties) {
            super(sender, renderer, properties);
        }

        @Override
        protected String createEmailText(NotificationContent content) {
            return "Custom: " + super.createEmailText(content);
        }
    }

    private void template(String text) {
        NotificationTemplate template = new NotificationTemplate();
        template.setName("test");
        template.setSubject("Notice");
        template.setText(text);
        when(repository.findByNameAndCompetentAuthority("test", CompetentAuthorityEnum.ENGLAND)).thenReturn(Optional.of(template));
    }

    private void stored(EmailData<EmailNotificationTemplateData> data, List<EmailFileLink> links) {
        when(providers.stream()).thenReturn(Stream.of(storage));
        when(storage.storeFiles(data.getLinkedFiles())).thenReturn(links);
    }

    private EmailData<EmailNotificationTemplateData> linkedData() {
        return data(Map.of(), Map.of("linked.pdf", new byte[]{1}));
    }

    private EmailData<EmailNotificationTemplateData> data(Map<String, byte[]> attachments, Map<String, byte[]> linkedFiles) {
        return EmailData.<EmailNotificationTemplateData>builder()
                .notificationTemplateData(EmailNotificationTemplateData.builder().templateName("test")
                        .competentAuthority(CompetentAuthorityEnum.ENGLAND).templateParams(Map.of("body", "Body")).build())
                .attachments(attachments).linkedFiles(linkedFiles).build();
    }

    private EmailFileLink file(String name, String url) {
        return EmailFileLink.builder().uuid(UUID.randomUUID()).fileName(name).url(URI.create(url)).build();
    }
}
