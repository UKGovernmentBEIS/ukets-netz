package uk.gov.netz.api.workflow.request.flow.rde.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.RequestTaskActionHandler;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeDecisionType;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeOutcome;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RdeResponseSubmitRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.rde.domain.RequestPayloadRdeable;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RdeResponseSubmitActionHandler
    implements RequestTaskActionHandler<RdeResponseSubmitRequestTaskActionPayload> {

    private final RequestTaskService requestTaskService;
    private final WorkflowService workflowService;


    @Override
    public RequestTaskPayload process(final Long requestTaskId,
                                      final String requestTaskActionType,
                                      final AppUser appUser,
                                      final RdeResponseSubmitRequestTaskActionPayload taskActionPayload) {

        // update request payload
        final RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);
        final Request request = requestTask.getRequest();
        final RequestPayloadRdeable requestPayload = (RequestPayloadRdeable) request.getPayload();
        requestPayload.getRdeData().setRdeDecisionPayload(taskActionPayload.getRdeDecisionPayload());

        final RdeDecisionType decision = taskActionPayload.getRdeDecisionPayload().getDecision();
        final RdeOutcome rdeOutcome = decision == RdeDecisionType.ACCEPTED ? RdeOutcome.ACCEPTED : RdeOutcome.REJECTED;

        // complete task
        workflowService.completeTask(
            requestTask.getProcessTaskId(),
            Map.of(BpmnProcessConstants.RDE_OUTCOME, rdeOutcome)
        );

        return requestTask.getPayload();
    }

    @Override
    public List<String> getTypes() {
        return List.of(RequestTaskActionTypes.RDE_RESPONSE_SUBMIT);
    }
}
