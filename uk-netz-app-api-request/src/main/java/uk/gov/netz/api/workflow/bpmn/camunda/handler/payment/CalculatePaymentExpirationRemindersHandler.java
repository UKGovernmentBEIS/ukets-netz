package uk.gov.netz.api.workflow.bpmn.camunda.handler.payment;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestExpirationKeys;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestExpirationVarsBuilder;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class CalculatePaymentExpirationRemindersHandler implements JavaDelegate {

    private final RequestExpirationVarsBuilder requestExpirationVarsBuilder;

    @Override
    public void execute(DelegateExecution execution) {
        final Date paymentExpirationDate = (Date) execution.getVariable(BpmnProcessConstants.PAYMENT_EXPIRATION_DATE);
        execution.setVariables(requestExpirationVarsBuilder.buildExpirationVars(RequestExpirationKeys.PAYMENT, paymentExpirationDate));
    }
}
