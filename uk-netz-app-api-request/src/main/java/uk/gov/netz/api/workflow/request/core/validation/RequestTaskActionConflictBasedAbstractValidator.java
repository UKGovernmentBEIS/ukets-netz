package uk.gov.netz.api.workflow.request.core.validation;

import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;

import java.util.Optional;
import java.util.Set;

public abstract class RequestTaskActionConflictBasedAbstractValidator implements RequestTaskActionValidator {

    protected abstract String getErrorCode();

    protected abstract Set<String> getConflictingRequestTaskTypes();

    @Override
    public RequestTaskActionValidationResult validate(final RequestTask requestTask) {
        final Set<String> requestTaskTypes = this.getConflictingRequestTaskTypes();

        Optional<RequestTask> conflictingRequestTask = requestTask.getRequest().getRequestTasks().stream()
            .filter(rt -> requestTaskTypes.contains(rt.getType().getCode()))
            .findFirst();

        return conflictingRequestTask.isEmpty()
            ? RequestTaskActionValidationResult.validResult()
            : RequestTaskActionValidationResult.invalidResult(this.getErrorCode());
    }
}
