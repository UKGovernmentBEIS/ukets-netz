package uk.gov.netz.api.workflow.request.core.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.common.exception.BusinessException;
import uk.gov.netz.api.common.exception.ErrorCode;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTaskActionValidatorService {

    private final List<RequestTaskActionValidator> validators;

    public void validate(final RequestTask requestTask,
                         final String requestTaskActionType) {

        List<RequestTaskActionValidationResult> validationResults = new ArrayList<>();

        validators.stream()
            .filter(v -> v.getTypes().contains(requestTaskActionType))
            .forEach(v -> validationResults.add(v.validate(requestTask)));

        boolean isValid = validationResults.stream().allMatch(RequestTaskActionValidationResult::isValid);

        if (!isValid) {
            throw new BusinessException(ErrorCode.REQUEST_TASK_ACTION_CANNOT_PROCEED, getValidationErrorCodes(validationResults));
        }
    }

    private Object[] getValidationErrorCodes(List<RequestTaskActionValidationResult> validationResults) {
        return validationResults.stream()
            .filter(validationResult -> !validationResult.isValid())
            .map(RequestTaskActionValidationResult::getErrorCode)
            .toArray();
    }
}
