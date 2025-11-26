package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestPayload;
import uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.domain.SystemMessageNotificationRequestTaskPayload;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemMessageNotificationInitializer implements InitializeRequestTaskHandler {

    private final RequestTaskTypeRepository requestTaskTypeRepository;

    @Override
    public RequestTaskPayload initializePayload(Request request) {
        SystemMessageNotificationRequestPayload requestPayload = (SystemMessageNotificationRequestPayload) request.getPayload();
        return SystemMessageNotificationRequestTaskPayload.builder()
            .payloadType(RequestTaskPayloadTypes.SYSTEM_MESSAGE_NOTIFICATION_PAYLOAD)
            .messagePayload(requestPayload.getMessagePayload())
            .build();
    }

    @Override
    public Set<String> getRequestTaskTypes() {
        return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.SYSTEM_NOTIFICATION).stream()
            .map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
