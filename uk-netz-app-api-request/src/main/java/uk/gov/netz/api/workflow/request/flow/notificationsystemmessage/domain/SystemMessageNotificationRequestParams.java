package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMessageNotificationRequestParams {

    private String requestTaskType;

    private Long accountId;

    private String notificationMessageRecipient;

    private NotificationContent notificationContent;
}
