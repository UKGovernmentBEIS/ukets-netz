package uk.gov.netz.api.notificationapi.mail.service;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EmailLinkedFileStorageServiceTest {

    private final EmailLinkedFileStorageService storageService = files -> files.keySet().stream()
        .sorted()
        .map(fileName -> EmailFileLink.builder()
            .uuid(UUID.nameUUIDFromBytes(fileName.getBytes(StandardCharsets.UTF_8)))
            .fileName(fileName)
            .url(URI.create("https://files.example.test/" + fileName))
            .build())
        .toList();

    @Test
    void emptyBulkInputReturnsNoLinks() {
        assertThat(storageService.storeFiles(Map.of())).isEmpty();
    }

    @Test
    void bulkResultHasOneLinkPerInputInFileNameOrder() {
        Map<String, byte[]> files = Map.of(
            "zeta.pdf", new byte[] {1},
            "alpha.pdf", new byte[] {2},
            "middle.pdf", new byte[] {3}
        );

        List<EmailFileLink> links = storageService.storeFiles(files);

        assertThat(links)
            .hasSize(files.size())
            .extracting(EmailFileLink::getFileName)
            .containsExactly("alpha.pdf", "middle.pdf", "zeta.pdf");
    }
}
