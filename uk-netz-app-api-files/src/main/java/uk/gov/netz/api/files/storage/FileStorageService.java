package uk.gov.netz.api.files.storage;

import java.util.List;

/**
 * Provider-neutral storage for files with library-assigned UUIDs.
 */
public interface FileStorageService {

    /**
     * Validates all uploads before storage and returns one result per input, in input order.
     * Empty input returns an empty list. A failed batch is cleaned up on a best-effort basis.
     *
     * @throws IllegalArgumentException if the destination or an upload is invalid
     * @throws NullPointerException if the destination or file list is null
     * @throws FileStorageException if storage fails; cleanup failures are suppressed on this exception
     */
    List<StoredFile> storeFiles(StorageDestination destination, List<FileUpload> files);

    /**
     * Deletes the exact stored reference without normalizing its path.
     *
     * @throws FileStorageException if deletion fails
     */
    void deleteFile(FileReference reference);
}
