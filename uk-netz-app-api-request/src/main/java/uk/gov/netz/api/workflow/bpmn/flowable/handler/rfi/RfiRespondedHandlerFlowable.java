package uk.gov.netz.api.workflow.bpmn.flowable.handler.rfi;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.rfi.service.RfiRespondedService;

@Service
@RequiredArgsConstructor
public class RfiRespondedHandlerFlowable implements JavaDelegate {

	private final RfiRespondedService service;

    @Override
    public void execute(DelegateExecution execution) {
        service.respond((String) execution.getVariable(BpmnProcessConstants.REQUEST_ID));
    }
}
