package uk.gov.netz.api.notificationapi.mail.constants;

import lombok.experimental.UtilityClass;

/**
 * Well-known names for email file metadata and rendering helpers supplied by the notification implementation.
 * Generated values take precedence for linked-file emails; otherwise existing caller values are preserved.
 */
@UtilityClass
public class EmailFileTemplateConstants {

    /** Sorted {@code List<EmailFileLink>} of stored files. */
    public static final String FILE_LINKS = "fileLinks";

    /** Sorted {@code List<String>} of MIME attachment names, without file contents. */
    public static final String ATTACHMENT_NAMES = "attachmentNames";

    /** FreeMarker helper: {@code downloadLink(file, optionalLabel)} produces an escaped Markdown link. */
    public static final String DOWNLOAD_LINK = "downloadLink";
}
