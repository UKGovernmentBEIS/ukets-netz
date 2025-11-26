package uk.gov.netz.api.workflow.request.flow.payment.handler;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskPayloadTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentTrackRequestTaskPayload;

@Service
@RequiredArgsConstructor
public class TrackPaymentInitializer implements InitializeRequestTaskHandler {

	private final RequestTaskTypeRepository requestTaskTypeRepository;
	
    @Override
    public RequestTaskPayload initializePayload(Request request) {
        return PaymentTrackRequestTaskPayload.builder()
            .payloadType(RequestTaskPayloadTypes.PAYMENT_TRACK_PAYLOAD)
            .amount(request.getPayload().getPaymentAmount())
            .paymentRefNum(request.getId())
            .build();
    }

    @Override
    public Set<String> getRequestTaskTypes() {
    	return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.TRACK_PAYMENT).stream()
				.map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
