package uk.gov.netz.api.workflow.request.core.validation;

import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;

import java.util.Set;

public interface RequestTaskActionValidator {
    
    RequestTaskActionValidationResult validate(RequestTask requestTask);

    Set<String> getTypes();
}
