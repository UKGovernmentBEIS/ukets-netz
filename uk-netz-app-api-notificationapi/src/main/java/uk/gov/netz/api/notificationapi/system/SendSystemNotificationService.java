package uk.gov.netz.api.notificationapi.system;

import uk.gov.netz.api.notificationapi.domain.NotificationContent;

public interface SendSystemNotificationService {

	void send(SystemNotificationInfo systemNotificationInfo, NotificationContent notificationContent);
}
