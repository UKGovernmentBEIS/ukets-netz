package uk.gov.netz.api.workflow.request.flow.common.taskhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskType;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskTypeRepository;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskCreateService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DefaultUserTaskCreatedHandler implements UserTaskCreatedHandler {

    protected final RequestTaskCreateService requestTaskCreateService;
    private final RequestTaskTypeRepository requestTaskTypeRepository;

    public void createRequestTask(final String requestId, final String processTaskId, final String taskDefinitionKey, final Map<String, Object> variables) {
        final String requestTaskTypeCode;
        final Optional<DynamicUserTaskDefinitionKey> dynamicKeyOpt = DynamicUserTaskDefinitionKey.fromString(taskDefinitionKey);

        if (dynamicKeyOpt.isPresent()) {
        	requestTaskTypeCode = resolveDynamicRequestTaskTypeCode(taskDefinitionKey, variables);
        } else {
        	requestTaskTypeCode = resolveFixedRequestTaskTypeCode(taskDefinitionKey);
        }
        
		final RequestTaskType requestTaskType = requestTaskTypeRepository.findByCode(requestTaskTypeCode)
				.orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_TASK_TYPE_NOT_FOUND));

        if (requestTaskType.isExpirable()) {
            final Date dueDate = (Date) variables.get(requestTaskType.getExpirationKey() + BpmnProcessConstants._EXPIRATION_DATE);
            final LocalDate dueDateLd = dueDate != null ? dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
            requestTaskCreateService.create(requestId, processTaskId, requestTaskType, null, dueDateLd);
        } else {
            requestTaskCreateService.create(requestId, processTaskId, requestTaskType);
        }
    }

    private String resolveDynamicRequestTaskTypeCode(final String taskDefinitionKey, final Map<String, Object> variables) {
        final String taskDefinitionKeyPrefix;

        // check if a specific prefix is defined in the bpmn variables
        final String bpmnPrefixVar = (String)variables.get(BpmnProcessConstants.REQUEST_TYPE_DYNAMIC_TASK_PREFIX);
        if (bpmnPrefixVar != null) {
            taskDefinitionKeyPrefix = bpmnPrefixVar;
        } else {
            // default case return the request type
            taskDefinitionKeyPrefix = (String) variables.get(BpmnProcessConstants.REQUEST_TYPE);
        }

        return taskDefinitionKeyPrefix + "_" + taskDefinitionKey;
    }

    private String resolveFixedRequestTaskTypeCode(final String taskDefinitionKey) {
        return taskDefinitionKey;
    }

}
