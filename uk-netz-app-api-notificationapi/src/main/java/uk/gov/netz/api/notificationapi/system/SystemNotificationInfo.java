package uk.gov.netz.api.notificationapi.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SystemNotificationInfo {

    private String template;
    private Map<String, Object> parameters;
    private Long accountId;
    private String receiver;
}
