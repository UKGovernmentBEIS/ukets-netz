package uk.gov.netz.api.workflow.request.core.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestTaskActionFileValidatorResolver {

	private final List<RequestTaskActionFileValidator> requestTaskActionFileValidators;

	public Set<RequestTaskActionFileValidator> resolve(String requestTaskActionType) {
		return requestTaskActionFileValidators.stream()
				.filter(validator -> validator.getRequestTaskActionTypes().contains(requestTaskActionType)).collect(Collectors.toSet());
	}
	
}
