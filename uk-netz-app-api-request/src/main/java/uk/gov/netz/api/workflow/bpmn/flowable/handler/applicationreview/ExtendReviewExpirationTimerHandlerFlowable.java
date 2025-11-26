package uk.gov.netz.api.workflow.bpmn.flowable.handler.applicationreview;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestExpirationKeys;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.service.ExtendExpirationTimerService;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestExpirationVarsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtendReviewExpirationTimerHandlerFlowable implements JavaDelegate {

	private final ExtendExpirationTimerService extendReviewExpirationTimerService;
    private final RequestExpirationVarsBuilder requestExpirationVarsBuilder;

    @Override
    public void execute(DelegateExecution execution) {
        final String requestId = (String) execution.getVariable(BpmnProcessConstants.REQUEST_ID);
		final LocalDate dueDateLocal = extendReviewExpirationTimerService.extendTimer(requestId,
				RequestExpirationKeys.APPLICATION_REVIEW);
        final Date dueDate = Date.from(dueDateLocal
                .atTime(LocalTime.MIN)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        Map<String, Object> expirationVars = requestExpirationVarsBuilder
                .buildExpirationVars(RequestExpirationKeys.APPLICATION_REVIEW, dueDate);
        execution.setVariables(expirationVars);
    }
    
}
