package uk.gov.netz.api.workflow.bpmn.flowable.handler.applicationreview;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestExpirationKeys;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskTimeManagementService;

@Service
@RequiredArgsConstructor
public class PauseReviewExpirationTimerHandlerFlowable implements JavaDelegate {

	private final RequestTaskTimeManagementService requestTaskTimeManagementService;

    @Override
    public void execute(DelegateExecution execution) {
        requestTaskTimeManagementService.pauseTasks((String) execution.getVariable(BpmnProcessConstants.REQUEST_ID),
        		RequestExpirationKeys.APPLICATION_REVIEW);
    }
}
