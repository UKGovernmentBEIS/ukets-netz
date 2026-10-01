package uk.gov.netz.api.files.storage.s3;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Operations;
import uk.gov.netz.api.files.common.utils.MimeTypeUtils;
import uk.gov.netz.api.files.storage.FileReference;
import uk.gov.netz.api.files.storage.FileStorageException;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.FileUpload;
import uk.gov.netz.api.files.storage.StorageDestination;
import uk.gov.netz.api.files.storage.StoredFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public final class S3FileStorageService implements FileStorageService {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    private final S3Operations operations;
    private final Supplier<UUID> uuidSupplier;
    private final BiFunction<byte[], String, String> mimeTypeDetector;

    public S3FileStorageService(S3Operations operations) {
        this(operations, UUID::randomUUID, MimeTypeUtils::detect);
    }

    S3FileStorageService(S3Operations operations, Supplier<UUID> uuidSupplier,
                         BiFunction<byte[], String, String> mimeTypeDetector) {
        this.operations = Objects.requireNonNull(operations, "operations must not be null");
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier, "uuidSupplier must not be null");
        this.mimeTypeDetector = Objects.requireNonNull(mimeTypeDetector, "mimeTypeDetector must not be null");
    }

    @Override
    public List<StoredFile> storeFiles(StorageDestination destination, List<FileUpload> files) {
        Objects.requireNonNull(destination, "destination must not be null");
        Objects.requireNonNull(files, "files must not be null");
        List<FileUpload> prepared = files.stream().map(this::prepare).toList();
        List<StoredFile> stored = new ArrayList<>();
        List<FileReference> attemptedUploads = new ArrayList<>();
        try {
            for (FileUpload file : prepared) {
                UUID uuid = Objects.requireNonNull(uuidSupplier.get(), "assigned UUID must not be null");
                FileReference reference = new FileReference(destination.getContainer(), destination.getPrefix() + "/" + uuid);
                ObjectMetadata metadata = ObjectMetadata.builder()
                        .contentLength((long) file.getContent().length)
                        .contentType(file.getContentType())
                        .contentDisposition(file.getContentDisposition())
                        .cacheControl(file.getCacheControl())
                        .build();
                StoredFile result = new StoredFile(uuid, reference, file.getFileName(),
                        file.getContentType(), file.getContent().length);
                // An upload may reach the provider even when its response fails to reach this caller.
                attemptedUploads.add(reference);
                operations.upload(reference.getContainer(), reference.getPath(), new ByteArrayInputStream(file.getContent()), metadata);
                stored.add(result);
            }
            return List.copyOf(stored);
        } catch (RuntimeException cause) {
            FileStorageException failure = new FileStorageException("File storage failed", cause);
            for (FileReference reference : attemptedUploads) {
                try {
                    deleteFile(reference);
                } catch (FileStorageException cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            throw failure;
        }
    }

    @Override
    public void deleteFile(FileReference reference) {
        Objects.requireNonNull(reference, "reference must not be null");
        try {
            operations.deleteObject(reference.getContainer(), reference.getPath());
        } catch (RuntimeException cause) {
            throw new FileStorageException("File deletion failed", cause);
        }
    }

    private FileUpload prepare(FileUpload file) {
        if (file == null || file.getFileName() == null || file.getFileName().isBlank() || file.getContent() == null) {
            throw new IllegalArgumentException("file name and content must be supplied");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = mimeTypeDetector.apply(file.getContent(), file.getFileName());
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = DEFAULT_CONTENT_TYPE;
        }
        return FileUpload.builder()
                .fileName(file.getFileName())
                .content(file.getContent())
                .contentType(contentType)
                .contentDisposition(file.getContentDisposition())
                .cacheControl(file.getCacheControl())
                .build();
    }
}
