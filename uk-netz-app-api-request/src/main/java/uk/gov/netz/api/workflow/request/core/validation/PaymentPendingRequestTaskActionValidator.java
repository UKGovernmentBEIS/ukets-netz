package uk.gov.netz.api.workflow.request.core.validation;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionValidationErrorCodes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskActionTypeRepository;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentPendingRequestTaskActionValidator implements RequestTaskActionValidator {

	private final RequestTaskActionTypeRepository requestTaskActionTypeRepository;
	
    @Override
    public RequestTaskActionValidationResult validate(final RequestTask requestTask) {

        final Boolean paymentCompleted = requestTask.getRequest().getPayload().getPaymentCompleted();
        return Boolean.TRUE.equals(paymentCompleted) ?
            RequestTaskActionValidationResult.validResult() :
            RequestTaskActionValidationResult.invalidResult(RequestTaskActionValidationErrorCodes.PAYMENT_IN_PROGRESS);
    }

    @Override
    public Set<String> getTypes() {
        Set<String> requestTaskActionTypes = new HashSet<>();
		requestTaskActionTypes.addAll(Set.of(RequestTaskActionTypes.RFI_SUBMIT,
				RequestTaskActionTypes.RDE_SUBMIT));
		requestTaskActionTypes.addAll(requestTaskActionTypeRepository.findAllByBlockedByPayment(true).stream()
				.map(RequestTaskActionType::getCode).collect(Collectors.toSet()));

        return requestTaskActionTypes;
    }
}
