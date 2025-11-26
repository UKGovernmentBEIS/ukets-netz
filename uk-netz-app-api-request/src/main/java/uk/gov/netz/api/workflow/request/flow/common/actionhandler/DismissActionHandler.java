package uk.gov.netz.api.workflow.request.flow.common.actionhandler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;

import java.util.List;
import java.util.Map;

/**
 * DismissActionHandler for closing request.
 */
@Component
@RequiredArgsConstructor
public class DismissActionHandler implements RequestTaskActionHandler<RequestTaskActionEmptyPayload> {

    private final WorkflowService workflowService;
    private final RequestTaskService requestTaskService;

    @Override
    public RequestTaskPayload process(final Long requestTaskId,
                                      final String requestTaskActionType,
                                      final AppUser appUser,
                                      final RequestTaskActionEmptyPayload payload) {

        final RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        workflowService.completeTask(requestTask.getProcessTaskId(),
            Map.of(BpmnProcessConstants.REQUEST_ID, requestTask.getRequest().getId()));

        return requestTask.getPayload();
    }

    @Override
    public List<String> getTypes() {
        return List.of(RequestTaskActionTypes.SYSTEM_MESSAGE_DISMISS);
    }
}