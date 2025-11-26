package uk.gov.netz.api.workflow.request.flow.payment.handler;

import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.InitializeRequestTaskHandler;
import uk.gov.netz.api.workflow.request.flow.payment.domain.RequestPayloadPayable;
import uk.gov.netz.api.workflow.request.flow.payment.transform.PaymentPayloadMapper;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfirmPaymentInitializer implements InitializeRequestTaskHandler {

	private final RequestTaskTypeRepository requestTaskTypeRepository;
    private static final PaymentPayloadMapper PAYMENT_PAYLOAD_MAPPER = Mappers.getMapper(PaymentPayloadMapper.class);

    @Override
    public RequestTaskPayload initializePayload(Request request) {
        RequestPayloadPayable requestPayloadPayable = (RequestPayloadPayable) request.getPayload();
        return PAYMENT_PAYLOAD_MAPPER.toConfirmPaymentRequestTaskPayload(request.getId(), requestPayloadPayable.getRequestPaymentInfo());
    }

    @Override
    public Set<String> getRequestTaskTypes() {
		return requestTaskTypeRepository.findAllByCodeEndingWith(RequestTaskTypes.CONFIRM_PAYMENT).stream()
				.map(RequestTaskType::getCode).collect(Collectors.toSet());
    }
}
