package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestTaskPayload;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemMessageNotificationInitializerTest {

    @InjectMocks
    private SystemMessageNotificationInitializer initializer;

    @Mock
    private RequestTaskTypeRepository requestTaskTypeRepository;

    @Test
    void initializePayload() {
        SystemMessageNotificationPayload systemMessageNotificationPayload = SystemMessageNotificationPayload.builder()
            .text("text")
            .subject("subject")
            .build();

        SystemMessageNotificationRequestPayload requestPayload = SystemMessageNotificationRequestPayload.builder()
            .payloadType(RequestPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_REQUEST_PAYLOAD)
            .messagePayload(systemMessageNotificationPayload)
            .build();

        Request request = Request.builder().payload(requestPayload).build();

        RequestTaskPayload requestTaskPayload = initializer.initializePayload(request);

        assertEquals(RequestTaskPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_PAYLOAD, requestTaskPayload.getPayloadType());
        assertThat(requestTaskPayload).isInstanceOf(SystemMessageNotificationRequestTaskPayload.class);
        assertEquals(systemMessageNotificationPayload, ((SystemMessageNotificationRequestTaskPayload) requestTaskPayload).getMessagePayload());
    }


    @Test
    void getRequestTaskTypes() {
        when(requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.SYSTEM_NOTIFICATION))
            .thenReturn(Set.of(RequestTaskType.builder().code("1SYSTEM_NOTIFICATION").build(),
                RequestTaskType.builder().code("2SYSTEM_NOTIFICATION").build()));

        assertEquals(initializer.getRequestTaskTypes(),
            Set.of("1SYSTEM_NOTIFICATION", "2SYSTEM_NOTIFICATION"));

        verify(requestTaskTypeRepository, times(1)).findAllByCodeEndingWith(RequestTaskTypes.SYSTEM_NOTIFICATION);
    }
}