package uk.gov.netz.api.notification.mail.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.files.storage.FileReference;
import uk.gov.netz.api.files.storage.FileStorageException;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.FileUpload;
import uk.gov.netz.api.files.storage.StorageDestination;
import uk.gov.netz.api.files.storage.StoredFile;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoredEmailLinkedFileStorageServiceTest {

    @Mock
    private FileStorageService storage;

    @Test
    void sanitizesAndOrdersUploadsAndUsesReturnedPaths() {
        when(storage.storeFiles(any(), anyList())).thenAnswer(invocation -> {
            List<FileUpload> uploads = invocation.getArgument(1);
            return uploads.stream().map(upload -> file(upload.getFileName(), "actual/" + upload.getFileName())).toList();
        });
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("z.pdf", new byte[]{3});
        files.put("b/a.pdf", new byte[]{2});
        files.put("a/a.pdf", new byte[]{1});
        files.put("C:\\unsafe\\safe\";\r\n.pdf", new byte[]{4});

        var links = service().storeFiles(files);

        assertThat(links).extracting(link -> link.getFileName()).containsExactly("a.pdf", "a.pdf", "safe.pdf", "z.pdf");
        assertThat(links.getFirst().getUrl()).isEqualTo(URI.create("https://files.example.gov.uk/base/actual/a.pdf"));
        ArgumentCaptor<StorageDestination> destination = ArgumentCaptor.forClass(StorageDestination.class);
        ArgumentCaptor<List<FileUpload>> uploads = ArgumentCaptor.captor();
        verify(storage).storeFiles(destination.capture(), uploads.capture());
        assertThat(destination.getValue()).isEqualTo(new StorageDestination("documents", "reports/email"));
        assertThat(uploads.getValue().getFirst().getContent()).containsExactly((byte) 1);
        assertThat(uploads.getValue().get(1).getContent()).containsExactly((byte) 2);
        assertThat(uploads.getValue()).allSatisfy(upload -> {
            assertThat(upload.getCacheControl()).isEqualTo("no-store");
            assertThat(upload.getContentType()).isNull();
            assertThat(upload.getContentDisposition()).startsWith("attachment;").contains("UTF-8").doesNotContain("\r", "\n");
        });
    }

    @Test
    void returnsStorageAssignedUuid() {
        StoredFile stored = file("report.txt", "reports/assigned");
        when(storage.storeFiles(any(), anyList())).thenReturn(List.of(stored));

        assertThat(service().storeFiles(Map.of("report.txt", new byte[]{1}))).singleElement()
                .satisfies(link -> assertThat(link.getUuid()).isEqualTo(stored.getUuid()));
    }

    @Test
    void rejectsInvalidEntriesBeforeStorage() {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("valid.txt", new byte[]{1});
        files.put("../", new byte[]{2});
        assertThatThrownBy(() -> service().storeFiles(files)).isInstanceOf(IllegalArgumentException.class);
        files.remove("../");
        files.put("missing.txt", null);
        assertThatThrownBy(() -> service().storeFiles(files)).isInstanceOf(NullPointerException.class);
        verifyNoInteractions(storage);
    }

    @Test
    void emptyInputSkipsStorage() {
        assertThat(service().storeFiles(Map.of())).isEmpty();
        verifyNoInteractions(storage);
    }

    @Test
    void delegatesUploadFailureWithoutAttemptingDuplicateCleanup() {
        FileStorageException failure = new FileStorageException("File storage failed", new IllegalStateException());
        when(storage.storeFiles(any(), anyList())).thenThrow(failure);
        assertThatThrownBy(() -> service().storeFiles(Map.of("a.txt", new byte[]{1}))).isSameAs(failure);
        verify(storage).storeFiles(any(), anyList());
        verifyNoMoreInteractions(storage);
    }

    @Test
    void linkFailureDeletesEveryStoredReferenceAndSuppressesCleanupFailure() {
        // A provider returning malformed result metadata must not leave the successfully uploaded batch behind.
        StoredFile first = file("first.txt", "actual/first");
        StoredFile second = new StoredFile(UUID.randomUUID(), null, "invalid.txt", "text/plain", 1);
        StoredFile third = file("third.txt", "actual/third");
        when(storage.storeFiles(any(), anyList())).thenReturn(List.of(first, second, third));
        FileStorageException cleanup = new FileStorageException("File deletion failed", new IllegalStateException());
        doThrow(cleanup).when(storage).deleteFile(first.getReference());

        assertThatThrownBy(() -> service().storeFiles(Map.of("a.txt", new byte[]{1})))
                .isInstanceOf(NullPointerException.class)
                .satisfies(failure -> assertThat(failure.getSuppressed()).containsExactly(cleanup));
        verify(storage).deleteFile(first.getReference());
        verify(storage).deleteFile(second.getReference());
        verify(storage).deleteFile(third.getReference());
    }

    @Test
    void validatesConfigurationAndPreservesLocalDefaultUrl() {
        EmailLinkProperties properties = new EmailLinkProperties();
        properties.setPrefix("application");
        StoredFile file = file("report.txt", "application/assigned");
        when(storage.storeFiles(any(), anyList())).thenReturn(List.of(file));
        assertThat(new StoredEmailLinkedFileStorageService(storage, properties)
                .storeFiles(Map.of("report.txt", new byte[]{1})).getFirst().getUrl().toString())
                .isEqualTo("http://uk-ets-files.s3.localhost.localstack.cloud:4566/application/assigned");
        properties.setPublicBaseUrl(URI.create("https://user@example.gov.uk?query=yes"));
        assertThatThrownBy(() -> new StoredEmailLinkedFileStorageService(storage, properties))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("notification.email-link.public-base-url");
    }

    private StoredEmailLinkedFileStorageService service() {
        EmailLinkProperties properties = new EmailLinkProperties();
        properties.setContainer("documents");
        properties.setPrefix(" /reports//email/ ");
        properties.setPublicBaseUrl(URI.create("https://files.example.gov.uk/base///"));
        return new StoredEmailLinkedFileStorageService(storage, properties);
    }

    private StoredFile file(String name, String path) {
        return new StoredFile(UUID.randomUUID(), new FileReference("documents", path), name, "text/plain", 1);
    }
}
