package uk.gov.netz.api.notification.system;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.notification.template.service.NotificationTemplateProcessService;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SendSystemNotificationService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;

@Service
@RequiredArgsConstructor
public class SystemNotificationProcessAndSendService {

	private final NotificationTemplateProcessService notificationTemplateProcessService;
	private final SendSystemNotificationService sendSystemNotificationService;
	
	public void processAndSend(SystemNotificationInfo msgInfo) {
		final NotificationContent notificationContent = process(msgInfo);
		sendSystemNotificationService.send(msgInfo, notificationContent);
	}
	
	private NotificationContent process(SystemNotificationInfo msgInfo) {
        return notificationTemplateProcessService
                .processNotificationTemplate(msgInfo.getTemplate(), msgInfo.getParameters());
    }
}
