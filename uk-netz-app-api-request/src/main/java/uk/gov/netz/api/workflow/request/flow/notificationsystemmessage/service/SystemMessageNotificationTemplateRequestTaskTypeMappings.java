package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import uk.gov.netz.api.verificationbody.NotificationTemplateName;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.constants.SystemMessageRequestTaskTypePrefixes;

import java.util.Map;

final class SystemMessageNotificationTemplateRequestTaskTypeMappings {

	private static final Map<String, String> SYSTEM_MESSAGE_NOTIFICATION_TEMPLATE_REQUEST_TASK_TYPE_PREFIX_MAP = Map.of(
        NotificationTemplateName.NEW_VERIFICATION_BODY,
            SystemMessageRequestTaskTypePrefixes.NEW_VERIFICATION_BODY,
        NotificationTemplateName.VERIFICATION_BODY_NO_LONGER_AVAILABLE,
            SystemMessageRequestTaskTypePrefixes.VERIFICATION_BODY_NO_LONGER_AVAILABLE);

	private SystemMessageNotificationTemplateRequestTaskTypeMappings() {}

	public static String getRequestTaskType(String notificationTemplateName) {
		return SYSTEM_MESSAGE_NOTIFICATION_TEMPLATE_REQUEST_TASK_TYPE_PREFIX_MAP.get(notificationTemplateName);
	}

	public static Map<String, String> getAllMappings() {
		return SYSTEM_MESSAGE_NOTIFICATION_TEMPLATE_REQUEST_TASK_TYPE_PREFIX_MAP;
	}
}
