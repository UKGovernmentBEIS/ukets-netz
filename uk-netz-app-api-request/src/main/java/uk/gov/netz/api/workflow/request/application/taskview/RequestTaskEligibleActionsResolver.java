package uk.gov.netz.api.workflow.request.application.taskview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionType;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskPayload;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class RequestTaskEligibleActionsResolver {

    private final List<RequestTaskActionEligibilityEvaluator<? extends RequestTaskPayload>> evaluators;

    public List<String> resolveEligibleRequestTaskActions(RequestTask requestTask) {
        return requestTask.getType().getActionTypes().stream().map(RequestTaskActionType::getCode)
                .filter(requestTaskActionCode -> evaluators.stream()
                        .filter(evaluator -> (evaluator.getRequestTaskType().equals(requestTask.getType().getCode())
                                && evaluator.getRequestTaskActionTypes().contains(requestTaskActionCode))).findFirst()
                        .map(evaluator -> ((RequestTaskActionEligibilityEvaluator<RequestTaskPayload>) evaluator).isEligible(requestTask.getPayload())).orElse(true))
                .collect(Collectors.toList());
    }
}
