package uk.gov.netz.api.notificationapi.mail.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.netz.api.competentauthority.CompetentAuthorityEnum;

import java.util.HashMap;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
public class EmailNotificationTemplateData {

    private CompetentAuthorityEnum competentAuthority;
    
    private String templateName;

    @Builder.Default
    private Map<String, Object> templateParams = new HashMap<>();
    
}
