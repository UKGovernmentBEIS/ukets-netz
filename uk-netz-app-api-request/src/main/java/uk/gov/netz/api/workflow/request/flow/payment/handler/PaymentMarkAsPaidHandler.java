package uk.gov.netz.api.workflow.request.flow.payment.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.authorization.core.domain.AppUser;
import uk.gov.netz.api.workflow.request.WorkflowService;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionTypes;
import uk.gov.netz.api.workflow.request.core.service.RequestTaskService;
import uk.gov.netz.api.workflow.request.flow.common.actionhandler.RequestTaskActionHandler;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.domain.RequestTaskActionEmptyPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentOutcome;
import uk.gov.netz.api.workflow.request.flow.payment.service.PaymentCompleteService;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentMarkAsPaidHandler implements RequestTaskActionHandler<RequestTaskActionEmptyPayload> {

    private final RequestTaskService requestTaskService;
    private final PaymentCompleteService paymentCompleteService;
    private final WorkflowService workflowService;

    @Override
    public RequestTaskPayload process(Long requestTaskId, String requestTaskActionType, AppUser appUser,
                        RequestTaskActionEmptyPayload payload) {
        RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        paymentCompleteService.markAsPaid(requestTask, appUser);

        workflowService.completeTask(requestTask.getProcessTaskId(),
            Map.of(BpmnProcessConstants.PAYMENT_OUTCOME, PaymentOutcome.MARK_AS_PAID));
        return requestTask.getPayload();
    }

    @Override
    public List<String> getTypes() {
    	return List.of(RequestTaskActionTypes.PAYMENT_MARK_AS_PAID);
    }
}
