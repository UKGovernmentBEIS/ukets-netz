package uk.gov.netz.api.documenttemplate.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentTemplateDTO {

    private Long id;
    private String name;
    private String workflow;
    private LocalDateTime lastUpdatedDate;
    
    private Long notificationTemplateId;

    private String fileUuid;
    private String filename;
    
}
