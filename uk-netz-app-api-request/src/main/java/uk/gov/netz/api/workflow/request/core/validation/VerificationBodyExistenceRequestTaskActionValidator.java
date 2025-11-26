package uk.gov.netz.api.workflow.request.core.validation;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionValidationErrorCodes;

import java.util.Set;

@Service
public abstract class VerificationBodyExistenceRequestTaskActionValidator extends
    RequestTaskActionConflictBasedAbstractValidator {

    @Override
    protected String getErrorCode() {
        return RequestTaskActionValidationErrorCodes.NO_VB_FOUND;
    }

    @Override
    public abstract Set<String> getTypes();

    @Override
    public abstract Set<String> getConflictingRequestTaskTypes();

    @Override
    public RequestTaskActionValidationResult validate(final RequestTask requestTask) {
        return ObjectUtils.isEmpty(requestTask.getRequest().getVerificationBodyId())
            ? RequestTaskActionValidationResult.invalidResult(this.getErrorCode())
            : RequestTaskActionValidationResult.validResult();
    }
}
