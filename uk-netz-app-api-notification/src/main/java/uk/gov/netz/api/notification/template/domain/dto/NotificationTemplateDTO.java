package uk.gov.netz.api.notification.template.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateDTO {

    private Long id;
    private String name;
    private String subject;
    private String text;
    private String eventTrigger;
    private String workflow;
    private LocalDateTime lastUpdatedDate;
    
}
