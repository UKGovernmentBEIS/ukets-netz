package uk.gov.netz.api.notification.template.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationTemplateSearchResults {

    private List<NotificationTemplateInfoDTO> templates;
    private Long total;
}
