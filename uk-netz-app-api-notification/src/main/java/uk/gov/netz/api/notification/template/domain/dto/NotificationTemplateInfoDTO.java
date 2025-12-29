package uk.gov.netz.api.notification.template.domain.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class NotificationTemplateInfoDTO {

    private Long id;
    private String name;
    private String roleType;
    private String workflow;
    private LocalDateTime lastUpdatedDate;
    
    public NotificationTemplateInfoDTO(Long id, String name, String roleType, String workflow, LocalDateTime lastUpdatedDate) {
        this.id = id;
        this.name = name;
        this.roleType = roleType;
        this.workflow = workflow;
        this.lastUpdatedDate = lastUpdatedDate;
    }
    
}
