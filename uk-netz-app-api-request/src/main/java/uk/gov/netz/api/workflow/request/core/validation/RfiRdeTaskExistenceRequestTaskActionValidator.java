package uk.gov.netz.api.workflow.request.core.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionValidationErrorCodes;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RfiRdeTaskExistenceRequestTaskActionValidator extends RequestTaskActionConflictBasedAbstractValidator {
	
	private final RequestTaskTypeRepository requestTaskTypeRepository;

    @Override
    public Set<String> getTypes() {
        return Set.of(RequestTaskActionTypes.RFI_SUBMIT, RequestTaskActionTypes.RDE_SUBMIT);
    }

    @Override
    public Set<String> getConflictingRequestTaskTypes() {
		return requestTaskTypeRepository
				.findAllByCodeEndingWithOrCodeEndingWith(RequestTaskTypes.WAIT_FOR_RFI_RESPONSE,
						RequestTaskTypes.WAIT_FOR_RDE_RESPONSE)
				.stream().map(RequestTaskType::getCode).collect(Collectors.toSet());
    }

    @Override
    protected String getErrorCode() {
        return RequestTaskActionValidationErrorCodes.RFI_RDE_ALREADY_EXISTS;
    }
}
