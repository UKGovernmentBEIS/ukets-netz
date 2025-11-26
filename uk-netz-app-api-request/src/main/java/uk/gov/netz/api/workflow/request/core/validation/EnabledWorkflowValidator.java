package uk.gov.netz.api.workflow.request.core.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.netz.api.workflow.request.core.config.FeatureFlagProperties;

@Service
@RequiredArgsConstructor
public class EnabledWorkflowValidator {

    private final FeatureFlagProperties featureFlagProperties;

    public boolean isWorkflowEnabled(String requestType) {
        return !featureFlagProperties.getDisabledWorkflows().contains(requestType);
    }
}
