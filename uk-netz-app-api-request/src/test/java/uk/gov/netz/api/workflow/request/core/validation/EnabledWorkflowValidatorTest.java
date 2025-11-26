package uk.gov.netz.api.workflow.request.core.validation;

import org.junit.jupiter.api.Test;
import uk.gov.netz.api.workflow.request.core.config.FeatureFlagProperties;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnabledWorkflowValidatorTest {

    @Test
    void isWorkflowNotAllowed() {
        String requestType = "DUMMY_REQUEST_TYPE";
        FeatureFlagProperties featureFlagProperties = new FeatureFlagProperties();
        featureFlagProperties.setDisabledWorkflows(Set.of(requestType));

        EnabledWorkflowValidator enabledWorkflowValidator = new EnabledWorkflowValidator(featureFlagProperties);

        boolean isAllowed = enabledWorkflowValidator.isWorkflowEnabled(requestType);

        assertFalse(isAllowed);
    }

    @Test
    void isWorkflowAllowed_when_all_workflows_enabled() {
        String requestType = "DUMMY_REQUEST_TYPE";
        FeatureFlagProperties featureFlagProperties = new FeatureFlagProperties();

        EnabledWorkflowValidator enabledWorkflowValidator = new EnabledWorkflowValidator(featureFlagProperties);

        boolean isAllowed = enabledWorkflowValidator.isWorkflowEnabled(requestType);

        assertTrue(isAllowed);

        isAllowed = enabledWorkflowValidator.isWorkflowEnabled(requestType);

        assertTrue(isAllowed);
    }

}
