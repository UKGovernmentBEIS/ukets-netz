package uk.gov.netz.api.files.storage;

import lombok.Builder;
import lombok.Getter;

/**
 * File content and optional HTTP metadata. The caller must not modify the content during storage.
 */
@Getter
@Builder
public final class FileUpload {

    private final String fileName;
    private final byte[] content;
    private final String contentType;
    private final String contentDisposition;
    private final String cacheControl;

}
