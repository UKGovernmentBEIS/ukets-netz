package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class EmailData<T extends EmailNotificationTemplateData> {
    
    private T notificationTemplateData;

    /**
     * MIME attachments, keyed by their logical filename.
     */
    @Builder.Default
    private Map<String, byte[]> attachments = new HashMap<>();

    /**
     * Files to render as download links, keyed by their logical filename. Each value contains the complete file
     * content. Linked files remain distinct from MIME attachments, irrespective of file size.
     */
    @Builder.Default
    private Map<String, byte[]> linkedFiles = new HashMap<>();

}
