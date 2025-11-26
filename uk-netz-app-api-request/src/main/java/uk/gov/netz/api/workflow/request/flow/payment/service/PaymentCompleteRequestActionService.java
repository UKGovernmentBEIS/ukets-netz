package uk.gov.netz.api.workflow.request.flow.payment.service;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestService;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPayloadPayable;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPaymentInfo;
import uk.gov.netz.api.workflow.request.flow.payment.transform.PaymentPayloadMapper;

@Service
@RequiredArgsConstructor
public class PaymentCompleteRequestActionService {

    private final RequestService requestService;
    private static final PaymentPayloadMapper PAYMENT_PAYLOAD_MAPPER = Mappers.getMapper(PaymentPayloadMapper.class);

    public void markAsPaid(String requestId) {
        addRequestActionSubmittedByPayer(
            requestId,
            RequestActionPayloadTypes.PAYMENT_MARKED_AS_PAID_PAYLOAD,
            RequestActionTypes.PAYMENT_MARKED_AS_PAID)
        ;
    }

    public void markAsReceived(String requestId) {
        Request request = requestService.findRequestById(requestId);
        RequestPayload requestPayload = request.getPayload();
        RequestPaymentInfo requestPaymentInfo = ((RequestPayloadPayable) requestPayload).getRequestPaymentInfo();

        requestService.addActionToRequest(
            request,
            PAYMENT_PAYLOAD_MAPPER.toPaymentProcessedRequestActionPayload(
                request.getId(),
                requestPaymentInfo,
                RequestActionPayloadTypes.PAYMENT_MARKED_AS_RECEIVED_PAYLOAD
            ),
            RequestActionTypes.PAYMENT_MARKED_AS_RECEIVED,
            requestPayload.getRegulatorAssignee()
        );
    }

    public void cancel(String requestId) {
        Request request = requestService.findRequestById(requestId);
        RequestPayload requestPayload = request.getPayload();
        RequestPaymentInfo requestPaymentInfo = ((RequestPayloadPayable) requestPayload).getRequestPaymentInfo();

        requestService.addActionToRequest(
            request,
            PAYMENT_PAYLOAD_MAPPER.toPaymentCancelledRequestActionPayload(requestPaymentInfo),
            RequestActionTypes.PAYMENT_CANCELLED,
            requestPayload.getRegulatorAssignee()
        );
    }

    public void complete(String requestId) {
        addRequestActionSubmittedByPayer(
            requestId,
            RequestActionPayloadTypes.PAYMENT_COMPLETED_PAYLOAD,
            RequestActionTypes.PAYMENT_COMPLETED
        );
    }

    private void addRequestActionSubmittedByPayer(String requestId, String requestActionPayloadType,
                                                  String requestActionType) {
        Request request = requestService.findRequestById(requestId);
        RequestPayloadPayable requestPayload = (RequestPayloadPayable) request.getPayload();
        RequestPaymentInfo requestPaymentInfo = requestPayload.getRequestPaymentInfo();


        requestService.addActionToRequest(
            request,
            PAYMENT_PAYLOAD_MAPPER.toPaymentProcessedRequestActionPayload(
                request.getId(),
                requestPaymentInfo,
                requestActionPayloadType
            ),
            requestActionType,
            requestPaymentInfo.getPaidById()
        );
    }
}
