package uk.gov.netz.api.files.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Stored file metadata with the library-assigned UUID and exact provider reference.
 */
@Value
@Builder
@AllArgsConstructor
public class StoredFile {

    UUID uuid;

    FileReference reference;

    String fileName;
    String contentType;
    long size;

}
