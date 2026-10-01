package uk.gov.netz.api.files.storage;

import lombok.Builder;
import lombok.Value;

/**
 * The exact location of a file within the configured provider. Its path may be a bearer credential.
 */
@Value
public class FileReference {

    String container;
    String path;

    @Builder
    public FileReference(String container, String path) {
        if (container == null || container.isBlank() || path == null || path.isBlank()) {
            throw new IllegalArgumentException("file reference container and path must not be blank");
        }
        this.container = container;
        this.path = path;
    }

}
