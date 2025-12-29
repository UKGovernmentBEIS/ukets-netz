package uk.gov.netz.api.notification.system;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SendSystemNotificationService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;

@Configuration
public class SendSystemNotificationServiceAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(SendSystemNotificationService.class)
	SendSystemNotificationService noOpSendSystemNotificationService() {
		return new SendSystemNotificationService() {
			@Override
			public void send(SystemNotificationInfo systemNotificationInfo, NotificationContent notificationContent) {
				// Do nothing
			}
		};
	}
}
