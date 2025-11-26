package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SendSystemNotificationService;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;
import uk.gov.netz.api.workflow.request.StartProcessRequestService;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestParams;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestPayload;

import java.util.Map;


@RequiredArgsConstructor
@Service
public class SendSystemMessageNotificationRequestService implements SendSystemNotificationService {

    private final StartProcessRequestService startProcessRequestService;
    private final UserRoleTypeService userRoleTypeService;
    
    @Override
    @Transactional
	public void send(SystemNotificationInfo systemNotificationInfo, NotificationContent notificationContent) {
    	SystemMessageNotificationRequestParams params = SystemMessageNotificationRequestParams.builder()
				.requestTaskType(SystemMessageNotificationTemplateRequestTaskTypeMappings.getRequestTaskType(systemNotificationInfo.getTemplate()))
                .accountId(systemNotificationInfo.getAccountId())
                .notificationMessageRecipient(systemNotificationInfo.getReceiver())
                .notificationContent(notificationContent)
                .build();

        SystemMessageNotificationRequestPayload requestPayload = createMessageNotificationRequestPayload(params);

        RequestParams requestParams = RequestParams.builder()
            .type(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .requestResources(Map.of(ResourceType.ACCOUNT, params.getAccountId().toString()))
            .requestPayload(requestPayload)
            .processVars(Map.of(BpmnProcessConstants.REQUEST_TYPE_DYNAMIC_TASK_PREFIX, params.getRequestTaskType()))
            .build();

        startProcessRequestService.startProcess(requestParams);
	}

    private SystemMessageNotificationRequestPayload createMessageNotificationRequestPayload(SystemMessageNotificationRequestParams params) {
        UserRoleTypeDTO recipientRoleType = userRoleTypeService.getUserRoleTypeByUserId(params.getNotificationMessageRecipient());

        NotificationContent requestNotificationContent = params.getNotificationContent();
        SystemMessageNotificationRequestPayload requestPayload = SystemMessageNotificationRequestPayload.builder()
            .payloadType(RequestPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_REQUEST_PAYLOAD)
            .messagePayload(SystemMessageNotificationPayload.builder()
                .subject(requestNotificationContent.getSubject())
                .text(requestNotificationContent.getText())
                .build()
            )
            .build();

        switch (recipientRoleType.getRoleType()) {
            case RoleTypeConstants.OPERATOR:
                requestPayload.setOperatorAssignee(params.getNotificationMessageRecipient());
                break;
            case RoleTypeConstants.REGULATOR:
                requestPayload.setRegulatorAssignee(params.getNotificationMessageRecipient());
                break;
            case RoleTypeConstants.VERIFIER:
                requestPayload.setVerifierAssignee(params.getNotificationMessageRecipient());
                break;
            default:
                throw new UnsupportedOperationException(String.format("Can not assign request to user with role type %s", recipientRoleType));
        }
        return requestPayload;
    }

}
