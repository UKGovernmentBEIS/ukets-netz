package uk.gov.netz.api.workflow.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestStatuses;
import uk.gov.netz.api.workflow.request.core.service.RequestCreateService;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.dto.RequestParams;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestIdGeneratorResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler for starting a workflow process.
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class StartProcessRequestService {

    private final WorkflowService workflowService;
    private final RequestCreateService requestCreateService;
    private final RequestIdGeneratorResolver requestIdGeneratorResolver;

    public Request startProcess(RequestParams params) {
        String requestId = generateRequestId(params);

        Request request = createRequest(params.withRequestId(requestId));

        Map<String, Object> processVars = new HashMap<>();
        processVars.put(BpmnProcessConstants.REQUEST_ID, request.getId());
        processVars.put(BpmnProcessConstants.REQUEST_TYPE, request.getType().getCode());
        processVars.putAll(params.getProcessVars());

        String processInstanceId = workflowService.startProcessDefinition(request, processVars);
        log.info("Triggered {} process flow with id: {}", () -> request.getType().getCode(), () -> processInstanceId);

        setProcessToRequest(processInstanceId, request);

        return request;
    }

    public void reStartProcess(Request request, Map<String, Object> customProcessVars) {
        Map<String, Object> processVars = new HashMap<>();
        processVars.put(BpmnProcessConstants.REQUEST_ID, request.getId());
        processVars.put(BpmnProcessConstants.REQUEST_TYPE, request.getType().getCode());
        processVars.putAll(customProcessVars);

        request.setStatus(RequestStatuses.IN_PROGRESS);
        String processInstanceId = workflowService.reStartProcessDefinition(request, processVars);
        log.info("Restarted {} process flow with id: {}", () -> request.getType().getCode(), () -> processInstanceId);

        setProcessToRequest(processInstanceId, request);
    }

    public void reStartProcess(Request request) {
        reStartProcess(request, Map.of());
    }

    private Request createRequest(RequestParams params) {
        return requestCreateService.createRequest(params, RequestStatuses.IN_PROGRESS);
    }

    private void setProcessToRequest(String processInstanceId, Request request) {
        request.setProcessInstanceId(processInstanceId);
    }

    private String generateRequestId(RequestParams params) {
        return requestIdGeneratorResolver.get(params.getType()).generate(params);
    }
}
