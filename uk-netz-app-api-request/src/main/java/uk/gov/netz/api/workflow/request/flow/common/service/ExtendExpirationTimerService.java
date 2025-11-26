package uk.gov.netz.api.workflow.request.flow.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExtendExpirationTimerService {

    private final RequestService requestService;
    private final RequestTaskTimeManagementService requestTaskTimeManagementService;

    public LocalDate extendTimer(final String requestId, final String expirationType) {
        final Request request = requestService.findRequestById(requestId);
        final RequestPayloadRdeable requestPayload = (RequestPayloadRdeable) request.getPayload();
        final LocalDate extensionDate = requestPayload.getRdeData().getRdePayload().getExtensionDate();

        requestTaskTimeManagementService.setDueDateToTasks(requestId, expirationType, extensionDate);

        return extensionDate;
    }
}
