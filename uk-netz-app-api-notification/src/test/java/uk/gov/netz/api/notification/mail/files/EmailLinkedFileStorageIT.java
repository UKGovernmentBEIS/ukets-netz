package uk.gov.netz.api.notification.mail.files;

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration;
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration;
import io.awspring.cloud.s3.S3Operations;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.notification.mail.service.JavaSendEmailServiceImpl;
import uk.gov.netz.api.notification.mail.service.NotificationEmailServiceImpl;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notification.template.CustomFreeMarkerConfiguration;
import uk.gov.netz.api.notification.template.domain.NotificationTemplate;
import uk.gov.netz.api.notification.template.repository.NotificationTemplateRepository;
import uk.gov.netz.api.notificationapi.mail.config.property.NotificationProperties;
import uk.gov.netz.api.notificationapi.mail.domain.EmailData;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;
import uk.gov.netz.api.notificationapi.mail.domain.EmailNotificationTemplateData;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;
import uk.gov.netz.api.notificationapi.mail.service.SendEmailService;

import uk.gov.netz.autoconfigure.files.S3FileStorageAutoConfiguration;
import uk.gov.netz.autoconfigure.notification.EmailFileStorageAutoConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@Testcontainers
class EmailLinkedFileStorageIT {

    private static final String BUCKET = "uk-ets-files";

    @Container
    private static final LocalStackContainer LOCALSTACK = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:4.8.1"))
            .withServices(S3);

    @Test
    void springCloudConfiguredClientStoresEmailFilesWithDownloadMetadata() {
        runner("https://files.example.gov.uk")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(S3Client.class).hasSingleBean(S3Operations.class);
                    assertThat(context).doesNotHaveBean("netzFilesAwsS3Client");
                    AwsCredentialsProvider credentials = context.getBean(AwsCredentialsProvider.class);
                    assertThat(credentials.resolveCredentials().accessKeyId()).isEqualTo("email-link-test");
                    assertThat(credentials.resolveCredentials().secretAccessKey()).isEqualTo("email-link-secret");
                    S3Operations operations = context.getBean(S3Operations.class);
                    operations.createBucket(BUCKET);
                    EmailLinkedFileStorageService emailService = context.getBean(EmailLinkedFileStorageService.class);
                    byte[] content = "email-linked content".getBytes(StandardCharsets.UTF_8);

                    List<EmailFileLink> links = emailService.storeFiles(Map.of("report.txt", content));

                    assertThat(links).singleElement().satisfies(link -> {
                        assertThat(link.getFileName()).isEqualTo("report.txt");
                        assertThat(link.getUrl().toString())
                                .isEqualTo("https://files.example.gov.uk/mrtm/" + link.getUuid());
                    });
                    String key = "mrtm/" + links.getFirst().getUuid();
                    var stored = context.getBean(S3Client.class).getObjectAsBytes(request -> request.bucket(BUCKET).key(key));
                    assertThat(stored.asByteArray()).isEqualTo(content);
                    assertThat(stored.response().contentLength()).isEqualTo(content.length);
                    assertThat(stored.response().contentType()).isEqualTo("text/plain");
                    assertThat(stored.response().contentDisposition()).contains("attachment;", "report.txt");
                    assertThat(stored.response().cacheControl()).isEqualTo("no-store");
                    operations.deleteObject(BUCKET, key);
                    assertThat(operations.objectExists(BUCKET, key)).isFalse();
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void notificationRendersStoredLinksAndKeepsMimeAttachmentsSeparate(boolean includeAttachment) {
        String publicBaseUrl = "https://files.example.gov.uk/base%2Fsegment";
        runner(publicBaseUrl + "///")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    S3Operations operations = context.getBean(S3Operations.class);
                    operations.createBucket(BUCKET);
                    NotificationProperties properties = new NotificationProperties();
                    NotificationProperties.Email emailProperties = new NotificationProperties.Email();
                    emailProperties.setAutoSender("sender@example.gov.uk");
                    properties.setEmail(emailProperties);
                    NotificationTemplateRepository repository = mock(NotificationTemplateRepository.class);
                    NotificationTemplate template = new NotificationTemplate();
                    template.setName("test");
                    template.setSubject("Notice");
                    template.setText("""
                            Notice body

                            <#if attachmentNames?has_content>Attached: ${attachmentNames?join(", ")}</#if>

                            Your documents:

                            <#list fileLinks as file>
                            - ${downloadLink(file)}
                            </#list>

                            Closing text.
                            """);
                    when(repository.findByNameAndCompetentAuthority("test", null)).thenReturn(Optional.of(template));
                    NotificationTemplateProcessService templates = new NotificationTemplateProcessService(
                            new CustomFreeMarkerConfiguration().freemarkerConfig(), repository);
                    JavaMailSender mailSender = mock(JavaMailSender.class);
                    MimeMessage message = new MimeMessage((Session) null);
                    when(mailSender.createMimeMessage()).thenReturn(message);
                    CompletableFuture<MimeMessage> sent = new CompletableFuture<>();
                    doAnswer(invocation -> {
                        MimeMessage outgoing = invocation.getArgument(0);
                        outgoing.saveChanges();
                        sent.complete(outgoing);
                        return null;
                    }).when(mailSender).send(message);
                    NotificationEmailServiceImpl notification = new NotificationEmailServiceImpl(
                            new JavaSendEmailServiceImpl(mailSender, properties), templates, properties,
                            context.getBeanProvider(EmailLinkedFileStorageService.class));
                    byte[] linkedContent = "linked report content".getBytes(StandardCharsets.UTF_8);
                    byte[] attachmentContent = "attachment content".getBytes(StandardCharsets.UTF_8);

                    notification.notifyRecipient(EmailData.<EmailNotificationTemplateData>builder()
                                    .notificationTemplateData(EmailNotificationTemplateData.builder()
                                            .templateName("test").templateParams(Map.of()).build())
                                    .linkedFiles(Map.of("reports/report & notes.txt", linkedContent))
                                    .attachments(includeAttachment ? Map.of("attachment.txt", attachmentContent) : Map.of())
                                    .build(),
                            "recipient@example.gov.uk");

                    MimeMessage delivered = sent.get(10, TimeUnit.SECONDS);
                    assertThat(delivered.getSubject()).isEqualTo("Notice");
                    MimeMultipart mixed = (MimeMultipart) delivered.getContent();
                    assertThat(mixed.getCount()).isEqualTo(includeAttachment ? 2 : 1);
                    MimeMultipart related = (MimeMultipart) mixed.getBodyPart(0).getContent();
                    assertThat(related.getCount()).isEqualTo(1);
                    assertThat(related.getBodyPart(0).isMimeType("text/html")).isTrue();
                    String html = (String) related.getBodyPart(0).getContent();
                    S3Client s3 = context.getBean(S3Client.class);
                    var objects = s3.listObjectsV2(request -> request.bucket(BUCKET).prefix("mrtm/")).contents();
                    assertThat(objects).hasSize(1);
                    String key = objects.getFirst().key();
                    try {
                        assertThat(html).contains("<p>Notice body</p>", "<p>Your documents:</p>",
                                "href=\"" + publicBaseUrl + "/" + key + "\">report &amp; notes.txt</a>");
                        assertThat(html.indexOf(publicBaseUrl)).isLessThan(html.indexOf("Closing text."));
                        assertThat(html).doesNotContain("Files shared with this email:");
                        var stored = s3.getObjectAsBytes(request -> request.bucket(BUCKET).key(key));
                        assertThat(stored.asByteArray()).isEqualTo(linkedContent);
                        assertThat(stored.response().contentDisposition()).contains("attachment;");
                        assertThat(stored.response().cacheControl()).isEqualTo("no-store");
                        if (includeAttachment) {
                            assertThat(html).contains("Attached: attachment.txt");
                            var attachment = mixed.getBodyPart(1);
                            assertThat(attachment.getDisposition()).isEqualTo(Part.ATTACHMENT);
                            assertThat(attachment.getFileName()).isEqualTo("attachment.txt");
                            assertThat(attachment.getInputStream().readAllBytes()).isEqualTo(attachmentContent);
                        } else {
                            assertThat(html).doesNotContain("Attached:");
                        }
                    } finally {
                        operations.deleteObject(BUCKET, key);
                    }
                });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void templateFailuresLeaveUploadedFilesInStorage(boolean missingTemplate) {
        String prefix = "render-failure/" + missingTemplate;
        runner("https://files.example.gov.uk")
                .withPropertyValues("notification.email-link.prefix=" + prefix)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    S3Operations operations = context.getBean(S3Operations.class);
                    operations.createBucket(BUCKET);
                    NotificationTemplateRepository repository = mock(NotificationTemplateRepository.class);
                    NotificationTemplate template = new NotificationTemplate();
                    template.setName("test");
                    template.setSubject("Notice");
                    template.setText("${missingParameter}");
                    when(repository.findByNameAndCompetentAuthority("test", null))
                            .thenReturn(missingTemplate ? Optional.empty() : Optional.of(template));
                    NotificationTemplateProcessService templates = new NotificationTemplateProcessService(
                            new CustomFreeMarkerConfiguration().freemarkerConfig(), repository);
                    SendEmailService sender = mock(SendEmailService.class);
                    NotificationEmailServiceImpl notification = new NotificationEmailServiceImpl(sender, templates,
                            new NotificationProperties(), context.getBeanProvider(EmailLinkedFileStorageService.class));
                    byte[] content = "retained document".getBytes(StandardCharsets.UTF_8);
                    EmailData<EmailNotificationTemplateData> data = EmailData.<EmailNotificationTemplateData>builder()
                            .notificationTemplateData(EmailNotificationTemplateData.builder().templateName("test").build())
                            .linkedFiles(Map.of("report.txt", content)).build();

                    assertThatThrownBy(() -> notification.notifyRecipient(data, "recipient@example.gov.uk"))
                            .isInstanceOf(BusinessException.class);

                    verifyNoInteractions(sender);
                    S3Client s3 = context.getBean(S3Client.class);
                    var objects = s3.listObjectsV2(request -> request.bucket(BUCKET).prefix(prefix + "/")).contents();
                    try {
                        assertThat(objects).hasSize(1);
                        byte[] retained = s3.getObjectAsBytes(request -> request.bucket(BUCKET).key(objects.getFirst().key()))
                                .asByteArray();
                        assertThat(retained).isEqualTo(content);
                    } finally {
                        objects.forEach(object -> operations.deleteObject(BUCKET, object.key()));
                    }
                });
    }

    private ApplicationContextRunner runner(String publicBaseUrl) {
        return new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(AwsAutoConfiguration.class, CredentialsProviderAutoConfiguration.class,
                        RegionProviderAutoConfiguration.class, S3AutoConfiguration.class,
                        S3FileStorageAutoConfiguration.class, EmailFileStorageAutoConfiguration.class))
                .withPropertyValues(
                        "spring.cloud.aws.endpoint=" + LOCALSTACK.getEndpointOverride(S3),
                        "spring.cloud.aws.credentials.access-key=email-link-test",
                        "spring.cloud.aws.credentials.secret-key=email-link-secret",
                        "spring.cloud.aws.region.static=" + LOCALSTACK.getRegion(),
                        "spring.cloud.aws.s3.path-style-access-enabled=true",
                        "notification.email-link.enabled=true",
                        "notification.email-link.container=" + BUCKET,
                        "notification.email-link.prefix=mrtm",
                        "notification.email-link.public-base-url=" + publicBaseUrl);
    }
}
