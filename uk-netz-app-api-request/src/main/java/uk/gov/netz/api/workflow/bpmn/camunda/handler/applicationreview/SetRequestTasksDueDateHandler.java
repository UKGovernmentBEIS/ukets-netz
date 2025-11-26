package uk.gov.netz.api.workflow.bpmn.camunda.handler.applicationreview;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import uk.gov.netz.api.workflow.request.core.domain.constants.RequestExpirationKeys;
import uk.gov.netz.api.workflow.request.flow.common.constants.BpmnProcessConstants;
import uk.gov.netz.api.workflow.request.flow.common.service.RequestTaskTimeManagementService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class SetRequestTasksDueDateHandler implements JavaDelegate {

    private final RequestTaskTimeManagementService requestTaskTimeManagementService;

    @Override
    public void execute(DelegateExecution execution) {
        final String requestId = (String) execution.getVariable(BpmnProcessConstants.REQUEST_ID);
        final LocalDate expirationDate = ((Date) execution
                .getVariable(BpmnProcessConstants.APPLICATION_REVIEW_EXPIRATION_DATE)).toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDate();
        requestTaskTimeManagementService
            .setDueDateToTasks(requestId, RequestExpirationKeys.APPLICATION_REVIEW, expirationDate);
    }
}
