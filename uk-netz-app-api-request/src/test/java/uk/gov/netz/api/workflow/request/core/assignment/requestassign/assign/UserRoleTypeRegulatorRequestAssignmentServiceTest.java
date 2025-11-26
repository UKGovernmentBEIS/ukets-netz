package uk.gov.netz.api.workflow.request.core.assignment.requestassign.assign;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.netz.api.common.constants.RoleTypeConstants;
import uk.gov.netz.api.workflow.request.TestRequestPayload;
import uk.gov.netz.api.workflow.request.core.domain.Request;

@ExtendWith(MockitoExtension.class)
class UserRoleTypeRegulatorRequestAssignmentServiceTest {

	@InjectMocks
    private UserRoleTypeRegulatorRequestAssignmentService cut;
	
	@Test
	void getRoleType() {
		assertThat(cut.getRoleType()).isEqualTo(RoleTypeConstants.REGULATOR);
	}
	
	@Test
	void assign() {
		TestRequestPayload payload = TestRequestPayload.builder().build();
		Request request = Request.builder()
				.payload(payload)
				.build();
		
		cut.assign(request, "userId");
		
		assertThat(payload.getRegulatorAssignee()).isEqualTo("userId");
	}
}
