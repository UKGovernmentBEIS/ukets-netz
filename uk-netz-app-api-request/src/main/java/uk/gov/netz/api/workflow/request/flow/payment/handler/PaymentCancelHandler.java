package uk.gov.netz.api.workflow.request.flow.payment.handler;

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
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentCancelRequestTaskActionPayload;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentOutcome;
import uk.gov.netz.api.workflow.request.flow.payment.domain.PaymentReviewOutcome;
import uk.gov.netz.api.workflow.request.flow.payment.service.PaymentCompleteService;

@Component
@RequiredArgsConstructor
public class PaymentCancelHandler implements RequestTaskCancelActionHandler<PaymentCancelRequestTaskActionPayload> {

    private final RequestTaskService requestTaskService;
    private final PaymentCompleteService paymentCompleteService;
    private final WorkflowService workflowService;

    @Override
    public void cancel(Long requestTaskId, PaymentCancelRequestTaskActionPayload payload, AppUser appUser) {
        RequestTask requestTask = requestTaskService.findTaskById(requestTaskId);

        paymentCompleteService.cancel(requestTask.getRequest(), payload.getReason());

        workflowService.completeTask(requestTask.getProcessTaskId(),
            Map.of(
                BpmnProcessConstants.PAYMENT_REVIEW_OUTCOME, PaymentReviewOutcome.CANCELLED,
                BpmnProcessConstants.PAYMENT_OUTCOME, PaymentOutcome.SUCCEEDED
            )
        );
    }

    @Override
    public List<String> getRequestTaskTypes() {
    	return List.of(RequestTaskTypes.TRACK_PAYMENT, RequestTaskTypes.CONFIRM_PAYMENT);
    }
}
