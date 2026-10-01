package uk.gov.netz.api.files.storage;

/**
 * A provider-neutral storage failure retaining its underlying cause.
 */
public class FileStorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
