package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class EmailData<T extends EmailNotificationTemplateData> {
    
    private T notificationTemplateData;
    
    @Builder.Default
    private Map<String, byte[]> attachments = new HashMap<>();
    
}
