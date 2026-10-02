package uk.gov.netz.api.notificationapi.mail.domain;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmailDataTest {

    @Test
    void builderDefaultsCollectionsToIndependentEmptyMaps() {
        EmailData<EmailNotificationTemplateData> first = EmailData.<EmailNotificationTemplateData>builder().build();
        EmailData<EmailNotificationTemplateData> second = EmailData.<EmailNotificationTemplateData>builder().build();

        assertThat(first.getAttachments()).isNotNull().isEmpty();
        assertThat(first.getLinkedFiles()).isNotNull().isEmpty();
        assertThat(first.getAttachments()).isNotSameAs(first.getLinkedFiles());
        assertThat(first.getAttachments()).isNotSameAs(second.getAttachments());
        assertThat(first.getLinkedFiles()).isNotSameAs(second.getLinkedFiles());

        first.getAttachments().put("attachment.txt", new byte[] {1});
        first.getLinkedFiles().put("linked-file.pdf", new byte[] {2});

        assertThat(second.getAttachments()).isEmpty();
        assertThat(second.getLinkedFiles()).isEmpty();
    }

    @Test
    void builderUsesExplicitlySuppliedMaps() {
        Map<String, byte[]> attachments = new HashMap<>();
        Map<String, byte[]> linkedFiles = new HashMap<>();

        EmailData<EmailNotificationTemplateData> emailData = EmailData.<EmailNotificationTemplateData>builder()
            .attachments(attachments)
            .linkedFiles(linkedFiles)
            .build();

        assertThat(emailData.getAttachments()).isSameAs(attachments);
        assertThat(emailData.getLinkedFiles()).isSameAs(linkedFiles);
    }

    @Test
    void attachmentsAndLinkedFilesCanCoexist() {
        byte[] attachment = {1, 2};
        byte[] linkedFile = {3, 4};

        EmailData<EmailNotificationTemplateData> emailData = EmailData.<EmailNotificationTemplateData>builder()
            .attachments(Map.of("attachment.txt", attachment))
            .linkedFiles(Map.of("report.pdf", linkedFile))
            .build();

        assertThat(emailData.getAttachments()).containsEntry("attachment.txt", attachment);
        assertThat(emailData.getLinkedFiles()).containsEntry("report.pdf", linkedFile);
    }
}
