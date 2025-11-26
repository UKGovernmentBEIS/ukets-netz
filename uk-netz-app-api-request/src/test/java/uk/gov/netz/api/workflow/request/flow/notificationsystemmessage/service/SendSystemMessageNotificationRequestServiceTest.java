package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.authorization.core.domain.dto.UserRoleTypeDTO;
import uk.gov.netz.api.authorization.core.service.UserRoleTypeService;
import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.notificationapi.domain.NotificationContent;
import uk.gov.netz.api.notificationapi.system.SystemNotificationInfo;
import uk.gov.netz.api.workflow.request.StartProcessRequestService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestPayload;

@ExtendWith(MockitoExtension.class)
class SendSystemMessageNotificationRequestServiceTest {

    @InjectMocks
    private SendSystemMessageNotificationRequestService cut;

    @Mock
    private StartProcessRequestService startProcessRequestService;

    @Mock
    private UserRoleTypeService userRoleTypeService;

    @Test
    void send() {
        final Long accountId = 1L;
        final String notificationMessageRecipient = "operId";
        final String notificationSubject = "subject";
        final String notificationText = "subject";

        final SystemNotificationInfo systemMessageNotificationInfo = SystemNotificationInfo.builder()
            .template(uk.gov.netz.api.verificationbody.NotificationTemplateName.NEW_VERIFICATION_BODY)
            .accountId(accountId)
            .receiver(notificationMessageRecipient)
            .build();
        final NotificationContent notificationContent = NotificationContent.builder()
            .subject(notificationSubject)
            .text(notificationText)
            .build();
        final UserRoleTypeDTO recipientUserRoleType = UserRoleTypeDTO.builder()
            .userId(notificationMessageRecipient)
            .roleType(RoleTypeConstants.OPERATOR)
            .build();

        final SystemMessageNotificationRequestPayload requestPayload = SystemMessageNotificationRequestPayload.builder()
            .payloadType(RequestPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_REQUEST_PAYLOAD)
            .messagePayload(SystemMessageNotificationPayload.builder()
                .subject(notificationSubject)
                .text(notificationText)
                .build())
            .operatorAssignee(notificationMessageRecipient)
            .build();

        final RequestParams requestParams = RequestParams.builder()
            .type(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .requestResources(Map.of(ResourceType.ACCOUNT, accountId.toString()))
            .requestPayload(requestPayload)
            .processVars(Map.of(BpmnProcessConstants.REQUEST_TYPE_DYNAMIC_TASK_PREFIX, "NEW_VERIFICATION_BODY"))
            .build();

        final Request request = Request.builder().id("1").creationDate(LocalDateTime.now()).build();

        //mock
        when(userRoleTypeService.getUserRoleTypeByUserId(notificationMessageRecipient)).thenReturn(recipientUserRoleType);
        when(startProcessRequestService.startProcess(requestParams)).thenReturn(request);

        //invoke
        cut.send(systemMessageNotificationInfo, notificationContent);

        verify(startProcessRequestService, times(1)).startProcess(requestParams);
    }
}
