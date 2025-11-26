package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestCreateActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;
import uk.gov.netz.api.workflow.request.core.validation.EnabledWorkflowValidator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestCreateActionResourceTypeDelegator<T extends RequestCreateActionPayload> {
    private final List<RequestCreateActionResourceTypeHandler<T>> resourceTypeHandlers;
    private final EnabledWorkflowValidator enabledWorkflowValidator;
    private final RequestTypeRepository requestTypeRepository;

    public RequestCreateActionResourceTypeHandler<T> getResourceTypeHandler(String requestTypeCode) {
        RequestType requestType = requestTypeRepository.findAllByCanCreateManually(true).stream()
                .filter(type -> type.getCode().equals(requestTypeCode))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_CREATE_ACTION_NOT_ALLOWED,
                        String.format("%s is not supported", requestTypeCode)));

        return resourceTypeHandlers.stream()
                .filter(handler -> enabledWorkflowValidator.isWorkflowEnabled(requestType.getCode()))
                .filter(handler -> handler.getResourceType().equals(requestType.getResourceType()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, requestType.getResourceType()));
    }
}
