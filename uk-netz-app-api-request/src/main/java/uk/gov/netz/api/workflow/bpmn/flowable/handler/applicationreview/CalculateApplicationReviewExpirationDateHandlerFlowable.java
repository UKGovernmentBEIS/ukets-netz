package uk.gov.netz.api.workflow.bpmn.flowable.handler.applicationreview;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestExpirationKeys;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.service.CalculateApplicationReviewExpirationDateService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestExpirationVarsBuilder;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculateApplicationReviewExpirationDateHandlerFlowable implements JavaDelegate {

	private final List<CalculateApplicationReviewExpirationDateService> reviewExpirationDateServices;
    private final RequestExpirationVarsBuilder requestExpirationVarsBuilder;

    @Override
    public void execute(DelegateExecution execution) {
        final String requestType = (String)execution.getVariable(BpmnProcessConstants.REQUEST_TYPE);
        final String expirateTypeApplicationReview = RequestExpirationKeys.APPLICATION_REVIEW;
        
        reviewExpirationDateServices.stream().filter(service -> service.getTypes().contains(requestType)).findFirst()
                .ifPresentOrElse(
                        service -> service.expirationDate().ifPresent(expirationDate ->
                                execution.setVariables(requestExpirationVarsBuilder
                                        .buildExpirationVars(expirateTypeApplicationReview, expirationDate))),
                        () -> execution.setVariables(requestExpirationVarsBuilder
                                .buildExpirationVars(expirateTypeApplicationReview))
                );
    }
    
}
