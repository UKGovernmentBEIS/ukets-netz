package uk.gov.netz.api.files.storage.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.model.S3Exception;
import uk.gov.netz.api.files.storage.FileReference;
import uk.gov.netz.api.files.storage.FileStorageException;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.FileUpload;
import uk.gov.netz.api.files.storage.StorageDestination;
import uk.gov.netz.api.files.storage.StoredFile;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceTest {

    private static final UUID FIRST = UUID.fromString("00000000-0000-4000-8000-000000000001");
    private static final UUID SECOND = UUID.fromString("00000000-0000-4000-8000-000000000002");
    private static final StorageDestination DESTINATION = new StorageDestination("documents", " /reports//annual/ ");

    @Mock
    private S3Operations operations;

    @Test
    void assignsUuidsAndPreservesInputOrderContentAndMetadata() throws Exception {
        FileUpload first = FileUpload.builder().fileName("z.pdf").content(new byte[]{1, 2})
                .contentType("application/pdf").contentDisposition("inline").cacheControl("max-age=60").build();
        List<StoredFile> stored = service().storeFiles(DESTINATION, List.of(first, upload("a.txt")));

        assertThat(stored).extracting(StoredFile::getUuid).containsExactly(FIRST, SECOND);
        assertThat(stored).extracting(StoredFile::getFileName).containsExactly("z.pdf", "a.txt");
        assertThat(stored).extracting(file -> file.getReference().getPath())
                .containsExactly("reports/annual/" + FIRST, "reports/annual/" + SECOND);
        assertThat(stored).extracting(StoredFile::getSize).containsExactly(2L, 1L);
        ArgumentCaptor<InputStream> contents = ArgumentCaptor.forClass(InputStream.class);
        ArgumentCaptor<ObjectMetadata> metadata = ArgumentCaptor.forClass(ObjectMetadata.class);
        verify(operations, times(2)).upload(eq("documents"), anyString(), contents.capture(), metadata.capture());
        assertThat(contents.getAllValues().getFirst().readAllBytes()).containsExactly((byte) 1, (byte) 2);
        assertThat(metadata.getAllValues().getFirst().getContentType()).isEqualTo("application/pdf");
        assertThat(metadata.getAllValues().getFirst().getContentLength()).isEqualTo(2L);
        assertThat(metadata.getAllValues().getFirst().getContentDisposition()).isEqualTo("inline");
        assertThat(metadata.getAllValues().getFirst().getCacheControl()).isEqualTo("max-age=60");
        assertThat(metadata.getAllValues().getLast().getContentType()).isEqualTo("text/plain");
        assertThat(metadata.getAllValues().getLast().getContentDisposition()).isNull();
        assertThat(metadata.getAllValues().getLast().getCacheControl()).isNull();
        assertThatThrownBy(() -> stored.clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void oneInstanceAcceptsIndependentDestinations() {
        FileStorageService storage = service();
        StoredFile first = storage.storeFiles(DESTINATION, List.of(upload("a.txt"))).getFirst();
        StoredFile second = storage.storeFiles(new StorageDestination("other", "elsewhere"), List.of(upload("b.txt"))).getFirst();

        assertThat(first.getReference()).isEqualTo(new FileReference("documents", "reports/annual/" + FIRST));
        assertThat(second.getReference()).isEqualTo(new FileReference("other", "elsewhere/" + SECOND));
        verify(operations).upload(eq("other"), eq("elsewhere/" + SECOND), any(InputStream.class), any(ObjectMetadata.class));
    }

    @Test
    void validatesEveryFileBeforeAnyUpload() {
        FileUpload invalid = FileUpload.builder().fileName("missing.txt").build();
        assertThatThrownBy(() -> service().storeFiles(DESTINATION, List.of(upload("valid.txt"), invalid)))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(operations);
    }

    @Test
    void emptyBatchDoesNotCallProvider() {
        assertThat(service().storeFiles(DESTINATION, List.of())).isEmpty();
        verifyNoInteractions(operations);
    }

    @Test
    void detectsMissingMimeTypeAndFallsBackWhenUnknown() {
        FileStorageService storage = new S3FileStorageService(operations, () -> FIRST, (bytes, name) -> null);
        assertThat(storage.storeFiles(DESTINATION, List.of(upload("unknown"))).getFirst().getContentType())
                .isEqualTo("application/octet-stream");
    }

    @Test
    void explicitMimeTypeSkipsDetection() {
        FileStorageService storage = new S3FileStorageService(operations, () -> FIRST, (bytes, name) -> {
            throw new AssertionError("Explicit MIME type must be preserved");
        });
        FileUpload file = FileUpload.builder().fileName("a").content(new byte[0]).contentType("text/plain").build();
        assertThat(storage.storeFiles(DESTINATION, List.of(file)).getFirst().getContentType()).isEqualTo("text/plain");
    }

    @Test
    void translatesUploadFailureAndRollsBackOnlyAttemptedUploads() {
        RuntimeException cause = S3Exception.builder().message("provider failure").build();
        when(operations.upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                .thenReturn(null).thenThrow(cause);

        assertThatThrownBy(() -> service().storeFiles(DESTINATION, List.of(upload("a"), upload("b"), upload("c"))))
                .isInstanceOf(FileStorageException.class).hasCause(cause).hasMessage("File storage failed");
        verify(operations).deleteObject("documents", "reports/annual/" + FIRST);
        verify(operations).deleteObject("documents", "reports/annual/" + SECOND);
        verify(operations, times(2)).upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verifyNoMoreInteractions(operations);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void cleansUpObjectsStoredBeforeAnUploadThrows(int failingUpload) {
        Set<String> providerObjects = new HashSet<>();
        AtomicInteger attempts = new AtomicInteger();
        RuntimeException cause = new IllegalStateException("response lost after storing object");
        when(operations.upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                .thenAnswer(invocation -> {
                    providerObjects.add(invocation.getArgument(1));
                    if (attempts.incrementAndGet() == failingUpload) {
                        throw cause;
                    }
                    return null;
                });
        doAnswer(invocation -> {
            providerObjects.remove(invocation.<String>getArgument(1));
            return null;
        }).when(operations).deleteObject(anyString(), anyString());

        assertThatThrownBy(() -> service().storeFiles(DESTINATION, List.of(upload("a"), upload("b"), upload("c"))))
                .isInstanceOf(FileStorageException.class).hasCause(cause);

        assertThat(providerObjects).isEmpty();
        verify(operations, times(failingUpload)).upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class));
        verify(operations, times(failingUpload)).deleteObject(eq("documents"), anyString());
        verifyNoMoreInteractions(operations);
    }

    @Test
    void continuesCleanupAndSuppressesEveryTranslatedFailure() {
        RuntimeException uploadFailure = new IllegalStateException("upload");
        RuntimeException firstCleanupFailure = new IllegalStateException("first cleanup");
        RuntimeException secondCleanupFailure = new IllegalStateException("second cleanup");
        when(operations.upload(anyString(), anyString(), any(InputStream.class), any(ObjectMetadata.class)))
                .thenReturn(null).thenThrow(uploadFailure);
        doThrow(firstCleanupFailure).when(operations).deleteObject("documents", "reports/annual/" + FIRST);
        doThrow(secondCleanupFailure).when(operations).deleteObject("documents", "reports/annual/" + SECOND);

        assertThatThrownBy(() -> service().storeFiles(DESTINATION, List.of(upload("a"), upload("b"))))
                .isInstanceOf(FileStorageException.class).hasCause(uploadFailure)
                .satisfies(failure -> assertThat(failure.getSuppressed())
                        .allSatisfy(cleanup -> assertThat(cleanup).isInstanceOf(FileStorageException.class))
                        .extracting(Throwable::getCause).containsExactly(firstCleanupFailure, secondCleanupFailure));
        verify(operations).deleteObject("documents", "reports/annual/" + FIRST);
        verify(operations).deleteObject("documents", "reports/annual/" + SECOND);
    }

    @Test
    void deletionPreservesExactPathAndTranslatesFailure() {
        FileReference reference = new FileReference("documents", "/exact//path/");
        RuntimeException cause = S3Exception.builder().message("delete failed").build();
        doThrow(cause).when(operations).deleteObject(reference.getContainer(), reference.getPath());

        assertThatThrownBy(() -> service().deleteFile(reference)).isInstanceOf(FileStorageException.class).hasCause(cause);
        verify(operations).deleteObject("documents", "/exact//path/");
    }

    private FileStorageService service() {
        AtomicInteger index = new AtomicInteger();
        List<UUID> uuids = List.of(FIRST, SECOND);
        return new S3FileStorageService(operations, () -> uuids.get(index.getAndIncrement()), (bytes, name) -> "text/plain");
    }

    private FileUpload upload(String fileName) {
        return FileUpload.builder().fileName(fileName).content(new byte[]{3}).build();
    }
}
