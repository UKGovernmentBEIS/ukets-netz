package uk.gov.netz.api.workflow.request.core.assignment.requestassign.release;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.Request;

@ExtendWith(MockitoExtension.class)
class UserRoleTypeRegulatorRequestReleaseServiceTest {

	@InjectMocks
    private UserRoleTypeRegulatorRequestReleaseService cut;
	
	@Test
	void getRoleType() {
		assertThat(cut.getRoleType()).isEqualTo(RoleTypeConstants.REGULATOR);
	}
	
	@Test
	void release() {
		TestRequestPayload payload = TestRequestPayload.builder()
				.regulatorAssignee("assignee")
				.build();
		Request request = Request.builder()
				.payload(payload)
				.build();
		
		cut.release(request, "assignee");
		
		assertThat(payload.getRegulatorAssignee()).isNull();
	}
	
	@Test
	void release_new_user_is_null() {
		TestRequestPayload payload = TestRequestPayload.builder()
				.regulatorAssignee("assignee")
				.build();
		Request request = Request.builder()
				.payload(payload)
				.build();
		
		cut.release(request, null);
		
		assertThat(payload.getRegulatorAssignee()).isNull();
	}
	
	@Test
	void release_new_user_is_empty() {
		TestRequestPayload payload = TestRequestPayload.builder()
				.regulatorAssignee("assignee")
				.build();
		Request request = Request.builder()
				.payload(payload)
				.build();
		
		cut.release(request, "");
		
		assertThat(payload.getRegulatorAssignee()).isNull();
	}
	
}
