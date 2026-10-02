package uk.gov.netz.api.notification.mail.files;

import lombok.Value;
import org.springframework.http.ContentDisposition;
import uk.gov.netz.api.files.common.utils.FileNameSanitizer;
import uk.gov.netz.api.files.storage.FileStorageService;
import uk.gov.netz.api.files.storage.FileUpload;
import uk.gov.netz.api.files.storage.StorageDestination;
import uk.gov.netz.api.files.storage.StoredFile;
import uk.gov.netz.api.notificationapi.mail.domain.EmailFileLink;
import uk.gov.netz.api.notificationapi.mail.service.EmailLinkedFileStorageService;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class StoredEmailLinkedFileStorageService implements EmailLinkedFileStorageService {

    private final FileStorageService storage;
    private final StorageDestination destination;
    private final PublicFileUrlFactory urlFactory;

    public StoredEmailLinkedFileStorageService(FileStorageService storage, EmailLinkProperties properties) {
        this.storage = Objects.requireNonNull(storage, "storage must not be null");
        Objects.requireNonNull(properties, "properties must not be null").validatePublicBaseUrl();
        try {
            this.destination = new StorageDestination(properties.getContainer(), properties.getPrefix());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("notification.email-link destination is invalid", exception);
        }
        this.urlFactory = new PublicFileUrlFactory(properties.getPublicBaseUrl());
    }

    @Override
    public List<EmailFileLink> storeFiles(Map<String, byte[]> files) {
        Objects.requireNonNull(files, "files must not be null");
        if (files.isEmpty()) {
            return List.of();
        }
        List<FileUpload> uploads = files.entrySet().stream()
                .map(entry -> new PreparedUpload(entry.getKey(), prepare(entry)))
                .sorted(Comparator.comparing((PreparedUpload prepared) -> prepared.getUpload().getFileName())
                        .thenComparing(PreparedUpload::getSourceFileName))
                .map(PreparedUpload::getUpload)
                .toList();
        List<StoredFile> stored = storage.storeFiles(destination, uploads);
        try {
            return stored.stream().map(file -> EmailFileLink.builder()
                            .uuid(file.getUuid())
                            .fileName(file.getFileName())
                            .url(urlFactory.create(file.getReference().getPath()))
                            .build())
                    .toList();
        } catch (RuntimeException failure) {
            for (StoredFile file : stored) {
                try {
                    storage.deleteFile(file.getReference());
                } catch (RuntimeException cleanupFailure) {
                    if (cleanupFailure != failure) {
                        failure.addSuppressed(cleanupFailure);
                    }
                }
            }
            throw failure;
        }
    }

    private FileUpload prepare(Map.Entry<String, byte[]> entry) {
        String fileName = FileNameSanitizer.sanitize(entry.getKey());
        return FileUpload.builder()
                .fileName(fileName)
                .content(Objects.requireNonNull(entry.getValue(), "file content must not be null"))
                .contentDisposition(ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .cacheControl("no-store")
                .build();
    }

    @Value
    private static class PreparedUpload {

        String sourceFileName;
        FileUpload upload;
    }
}
