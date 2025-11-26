package uk.gov.netz.api.workflow.request.flow.rde.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeDecisionType;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeRejectedRequestActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;

@Service
@RequiredArgsConstructor
public class RdeRespondedService {

    private final RequestService requestService;

    public void respond(final String requestId) {

        final Request request = requestService.findRequestById(requestId);
        final RequestPayload requestPayload = request.getPayload();
        final RequestPayloadRdeable requestPayloadRdeable = (RequestPayloadRdeable) requestPayload;
        final String reason = requestPayloadRdeable.getRdeData().getRdeDecisionPayload().getReason();

        final boolean isAccepted = requestPayloadRdeable.getRdeData().getRdeDecisionPayload().getDecision() == RdeDecisionType.ACCEPTED;
        final String actionType = isAccepted ? RequestActionTypes.RDE_ACCEPTED : RequestActionTypes.RDE_REJECTED;
        final RequestActionPayload timelinePayload = isAccepted ? null :
            RdeRejectedRequestActionPayload
                .builder()
                .payloadType(RequestActionPayloadTypes.RDE_REJECTED_PAYLOAD)
                .reason(reason)
                .build();

        requestService.addActionToRequest(request,
            timelinePayload,
            actionType,
            requestPayload.getOperatorAssignee());
    }
}
