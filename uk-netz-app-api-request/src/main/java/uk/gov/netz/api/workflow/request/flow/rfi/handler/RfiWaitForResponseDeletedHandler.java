package uk.gov.netz.api.workflow.request.flow.rfi.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.DynamicUserTaskDefinitionKey;
import uk.gov.netz.api.workflow.request.flow.common.taskhandler.DynamicUserTaskDeletedHandler;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiCancelledService;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RfiWaitForResponseDeletedHandler implements DynamicUserTaskDeletedHandler {

    private final RfiCancelledService rfiCancelledService;

    @Override
    public void process(final String requestId, final Map<String, Object> variables) {

        if (!variables.containsKey(BpmnProcessConstants.RFI_OUTCOME)) {
            rfiCancelledService.cancel(requestId);
        }
    }

    @Override
    public DynamicUserTaskDefinitionKey getTaskDefinition() {
        return DynamicUserTaskDefinitionKey.WAIT_FOR_RFI_RESPONSE;
    }
}
