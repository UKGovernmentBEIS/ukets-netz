package uk.gov.netz.api.workflow.request.flow.common.service.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.userinfoapi.UserInfoDTO;

import java.util.Date;

@Data
@EqualsAndHashCode
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateExpirationReminderParams {

    private String workflowTask;
    
    private UserInfoDTO recipient;
    
    private String expirationTime;
    private String expirationTimeLong;
    private Date deadline;
    
}
