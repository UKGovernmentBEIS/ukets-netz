package uk.gov.netz.api.workflow.request.core.validation;

import org.junit.jupiter.api.Test;

import uk.gov.netz.api.authorization.rules.domain.ResourceType;
import uk.gov.netz.api.workflow.request.core.domain.Request;
import uk.gov.netz.api.workflow.request.core.domain.RequestResource;
import uk.gov.netz.api.workflow.request.core.domain.RequestTask;
import uk.gov.netz.api.workflow.request.core.domain.RequestTaskActionValidationResult;
import uk.gov.netz.api.workflow.request.core.domain.constants.RequestTaskActionValidationErrorCodes;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VerificationBodyExistenceRequestTaskActionValidatorTest {
    private static final String REQUEST_TASK_ACTION_TYPE = "TEST";
    private static final String CONFLICTING_REQUEST_TASK_TYPE = "TEST";

    private final TestVerificationBodyExistenceRequestTaskActionValidator validator =
        new TestVerificationBodyExistenceRequestTaskActionValidator();

    @Test
    void getErrorCode() {
        assertThat(validator.getErrorCode()).isEqualTo(RequestTaskActionValidationErrorCodes.NO_VB_FOUND);
    }

    @Test
    void getTypes() {
        assertThat(validator.getTypes()).isEqualTo(Set.of(REQUEST_TASK_ACTION_TYPE));
    }

    @Test
    void getConflictingRequestTaskTypes() {
        assertEquals(Set.of(CONFLICTING_REQUEST_TASK_TYPE), validator.getConflictingRequestTaskTypes());
    }

    @Test
    void validate() {
    	Request request = Request.builder().build();
    	addVbResourceToRequest(1L, request);
        final RequestTask requestTask = RequestTask.builder()
            .request(request)
            .build();

        assertEquals(RequestTaskActionValidationResult.validResult(), validator.validate(requestTask));
    }

    @Test
    void validate_no_vb() {
        final RequestTask requestTask = RequestTask.builder()
            .request(Request.builder().build())
            .build();
        assertEquals(RequestTaskActionValidationResult.invalidResult(RequestTaskActionValidationErrorCodes.NO_VB_FOUND),
            validator.validate(requestTask));
    }

    private void addVbResourceToRequest(Long vbId, Request request) {
		RequestResource vbIdResource = RequestResource.builder()
				.resourceType(ResourceType.VERIFICATION_BODY)
				.resourceId(vbId.toString())
				.request(request)
				.build();
        request.getRequestResources().add(vbIdResource);
	}

    private static class TestVerificationBodyExistenceRequestTaskActionValidator extends VerificationBodyExistenceRequestTaskActionValidator {

        @Override
        public Set<String> getTypes() {
            return Set.of(REQUEST_TASK_ACTION_TYPE);
        }

        @Override
        public Set<String> getConflictingRequestTaskTypes() {
            return Set.of(CONFLICTING_REQUEST_TASK_TYPE);
        }
    }

}

