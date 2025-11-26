package uk.gov.netz.api.workflow.request.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.core.validation.EnabledWorkflowValidator;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestCreateValidationResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailableRequestService {

	private final List<AvailableRequestResourceTypeHandler> availableRequestResourceTypeHandlers;
	private final RequestTypeRepository requestTypeRepository;
	private final EnabledWorkflowValidator enabledWorkflowValidator;

    @Transactional
	public Map<String, RequestCreateValidationResult> getAvailableWorkflows(final String resourceId, 
			final String resourceType, final AppUser appUser) {
		final Set<String> allManuallyCreateCreateRequestTypes = 
				requestTypeRepository.findAllByCanCreateManuallyAndResourceType(true, resourceType).stream()
				.map(RequestType::getCode)
				.filter(enabledWorkflowValidator::isWorkflowEnabled)
				.collect(Collectors.toSet());

        return availableRequestResourceTypeHandlers.stream()
        		.filter(handler -> handler.getResourceType().equals(resourceType))
        		.findFirst()
        		.map(handler -> handler.getAvailableRequestsForResource(resourceId, allManuallyCreateCreateRequestTypes, appUser))
                .orElseThrow(() -> {
                    throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
                });
    }

}
