package uk.gov.netz.api.workflow.request.flow.notificationsystemmessage.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.RequestType;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTypes;
import uk.gov.netz.api.workflow.request.core.repository.RequestTaskRepository;
import uk.gov.netz.api.workflow.request.core.repository.RequestTypeRepository;


@RequiredArgsConstructor
@Service
public class SystemMessageNotificationRequestService {

    private final RequestTaskRepository requestTaskRepository;
    private final WorkflowService workflowService;
    private final RequestTypeRepository requestTypeRepository;
    
    public void completeOpenSystemMessageNotificationRequests(String assignee) {
        RequestType requestType = requestTypeRepository.findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        requestTaskRepository
            .findByRequestTypeAndAssignee(requestType, assignee)
            .forEach(rt -> workflowService.completeTask(rt.getProcessTaskId()));
    }

    public void completeOpenSystemMessageNotificationRequests(String assignee, Long accountId) {
        RequestType requestType = requestTypeRepository.findByCode(RequestTypes.SYSTEM_MESSAGE_NOTIFICATION)
            .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND));

        requestTaskRepository.findByRequestTypeAndAssigneeAndRequestAccountId(
                requestType, assignee, accountId)
            .forEach(rt -> workflowService.completeTask(rt.getProcessTaskId()));
    }

}
