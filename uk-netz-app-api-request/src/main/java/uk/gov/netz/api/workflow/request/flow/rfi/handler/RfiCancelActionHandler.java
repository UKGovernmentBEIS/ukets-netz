package uk.gov.netz.api.workflow.request.flow.rfi.handler;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.RequestTaskCancelActionHandler;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.rfi.domain.RfiOutcome;

@Component
@RequiredArgsConstructor
public class RfiCancelActionHandler implements RequestTaskCancelActionHandler<RequestTaskActionEmptyPayload> {

    private final WorkflowService workflowService;
    private final RequestTaskService requestTaskService;

	@Override
	public void cancel(final Long requestTaskId, final RequestTaskActionEmptyPayload payload, final AppUser appUser) {
        final RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);
        workflowService.completeTask(requestTask.getProcessTaskId(),
                                     Map.of(BpmnProcessConstants.RFI_OUTCOME, RfiOutcome.CANCELLED));
    }

    @Override
    public List<String> getRequestTaskTypes() {
        return List.of(RequestTaskTypes.WAIT_FOR_RFI_RESPONSE);
    }
}
